package com.chrizlove.vortexpay.operations.repository;

import com.chrizlove.vortexpay.operations.entity.SettlementPayment;
import com.chrizlove.vortexpay.operations.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {
}
