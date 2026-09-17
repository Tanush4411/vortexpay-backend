package com.chrizlove.vortexpay.payment_service.outbox;

import com.chrizlove.vortexpay.common_lib.enums.EventAggregateType;
import com.chrizlove.vortexpay.payment_service.entity.OutboxEvent;
import com.chrizlove.vortexpay.payment_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    public void publish(EventAggregateType aggregateType, UUID aggregateId, String eventType, Map<String, Object> payload) {
    OutboxEvent outboxEvent=OutboxEvent.builder()
            .aggregateId(aggregateId)
            .aggregateType(aggregateType)
            .eventType(eventType)
            .payload(payload)
            .build();

    outboxEventRepository.save(outboxEvent);
    }
}
