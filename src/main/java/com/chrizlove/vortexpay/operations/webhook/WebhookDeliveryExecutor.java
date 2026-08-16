package com.chrizlove.vortexpay.operations.webhook;

import com.chrizlove.vortexpay.common.enums.WebhookEventStatus;
import com.chrizlove.vortexpay.operations.entity.WebhookEvent;
import com.chrizlove.vortexpay.operations.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryExecutor {

    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue webhookRetryQueue;
    private final RestClient restClient;
    private final WebhookDlqRecorder  dlqRecorder;

    private static final List<Duration> BACKOFF = List.of(
            Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(30),
            Duration.ofHours(2), Duration.ofHours(8), Duration.ofHours(24));

    private final int MAX_ATTEMPTS = 7;

    @Value("${webhook.delivery.signature-header:X-VortexPay-Signature}")
    private String signatureHeader;

    @Transactional
    public void deliver(UUID webhookEventId){
        Optional<WebhookEvent> webhookEvent = webhookEventRepository.findById(webhookEventId);
        if(webhookEvent.isEmpty()){
            log.warn("No webhook event with id {} found", webhookEventId);
            return;
        }

        WebhookEvent event=webhookEvent.get();
        if(event.getWebhookEventStatus()== WebhookEventStatus.DELIVERED || event.getWebhookEventStatus()==WebhookEventStatus.DEAD){
            log.warn("Cannot deliver the event {} in status: {}", webhookEventId, event.getWebhookEventStatus());
        }
        event.setAttempts(event.getAttempts()+1);
        event.setLastAttemptAt(LocalDateTime.now());

        try {
            var response=restClient.post()
                    .uri(event.getTargetUrl())
                    .header(signatureHeader, event.getSignature())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("event", event.getEventType(),"payload", event.getPayload()))
                    .retrieve().toBodilessEntity();

            int statusCode=response.getStatusCode().value();
            event.setLastResponseCode(statusCode);

            if(response.getStatusCode().is2xxSuccessful()){
                event.setWebhookEventStatus(WebhookEventStatus.DELIVERED);
                event.setDeliveredAt(LocalDateTime.now());
                webhookEventRepository.save(event);
                log.info("Successfully delivered webhook event with id: {}", event);
                return;
            }

            handleAttemptFailed(event,"HTTP"+statusCode);

        }catch (Exception e){
            event.setLastResponseBody(e.getMessage());
            handleAttemptFailed(event,e.getMessage());
            log.error("Got RestClientException", e);
        }
    }

    private void handleAttemptFailed(WebhookEvent event, String error) {
        event.setLastResponseBody(error);

        if(event.getAttempts()>=MAX_ATTEMPTS){
            event.setWebhookEventStatus(WebhookEventStatus.DEAD);
            dlqRecorder.recordAfterAttemptsExhausted(event,error);
            return;
        }

        Duration backoff=BACKOFF.get(event.getAttempts()-1);
        LocalDateTime nextRetryAt=LocalDateTime.now().plus(backoff);
        event.setWebhookEventStatus(WebhookEventStatus.FAILED);
        event.setLastAttemptAt(nextRetryAt);
        webhookEventRepository.save(event);

        webhookRetryQueue.enqueue(event.getId(),nextRetryAt);

        log.error("Handling attempt failed for webhook event with id: {}, with attempts: {}, Next Retry At: {}"
                , event.getId(), event.getAttempts(), nextRetryAt);
    }
}
