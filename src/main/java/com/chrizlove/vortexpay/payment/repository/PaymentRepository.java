package com.chrizlove.vortexpay.payment.repository;

import com.chrizlove.vortexpay.common.enums.PaymentStatus;
import com.chrizlove.vortexpay.payment.entity.OrderRecord;
import com.chrizlove.vortexpay.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderRecord_OrderId(OrderRecord order);

    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    List<Payment> findByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime globalWindow);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = : paymentId and p.merchantId = :merchantId")
    Optional<Payment> findByIdAndMerchantIdForUpdate(UUID paymentId, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = : paymentId")
    Optional<Payment> findByIdForUpdate(UUID paymentId);
}
