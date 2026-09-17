package com.chrizlove.vortexpay.payment_service.repository;

import com.chrizlove.vortexpay.common_lib.enums.OutboxStatus;
import com.chrizlove.vortexpay.payment_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
