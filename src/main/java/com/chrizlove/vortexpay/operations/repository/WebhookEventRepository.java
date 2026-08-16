package com.chrizlove.vortexpay.operations.repository;

import com.chrizlove.vortexpay.common.enums.WebhookEventStatus;
import com.chrizlove.vortexpay.operations.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    List<WebhookEvent> findByWebhookEventStatusAndNextRetryAtBefore(WebhookEventStatus webhookEventStatus, LocalDateTime now);
}
