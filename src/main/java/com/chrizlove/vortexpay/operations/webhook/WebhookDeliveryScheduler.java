package com.chrizlove.vortexpay.operations.webhook;

import com.chrizlove.vortexpay.common.enums.WebhookEventStatus;
import com.chrizlove.vortexpay.operations.entity.WebhookEvent;
import com.chrizlove.vortexpay.operations.repository.WebhookEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookDeliveryScheduler {

    private final WebhookRetryQueue retryQueue;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookDeliveryExecutor deliveryExecutor;

    private ExecutorService virtualThreadExecutor;

    @PostConstruct
    void init(){
        virtualThreadExecutor= Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    void shutdown(){
        virtualThreadExecutor.shutdown();
    }

    @Value("${app.webhook.delivery.poll-batch-size:100}")
    private int batchSize=100;


    @Scheduled(fixedDelay = 5000)
    public void pollAndDeliver(){
        Set<UUID> due= retryQueue.pollDue(batchSize);
        if(due.isEmpty()) return;

        for(UUID webhookEventId: due){
            virtualThreadExecutor.submit(()-> {deliveryExecutor.deliver(webhookEventId);});
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void reconcileFromDatabase(){
        LocalDateTime now = LocalDateTime.now();
        List<WebhookEvent> due= webhookEventRepository.
                findByWebhookEventStatusAndNextRetryAtBefore(WebhookEventStatus.PENDING, now);

        for(WebhookEvent webhookEvent: due){
            retryQueue.enqueueIfAbsent(webhookEvent.getId(),webhookEvent.getNextRetryAt());
        }
    }
}
