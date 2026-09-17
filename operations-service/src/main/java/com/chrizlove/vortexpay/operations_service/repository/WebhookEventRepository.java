package com.chrizlove.vortexpay.operations_service.repository;

import com.chrizlove.vortexpay.common_lib.enums.WebhookEventStatus;
import com.chrizlove.vortexpay.operations_service.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    List<WebhookEvent> findByWebhookEventStatusAndNextRetryAtBefore(WebhookEventStatus webhookEventStatus, LocalDateTime now);
}
