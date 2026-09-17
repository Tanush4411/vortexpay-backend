package com.chrizlove.vortexpay.payment_service.repository;

import com.chrizlove.vortexpay.payment_service.entity.OrderRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderRecord, UUID> {
    boolean existsByMerchantIdAndReceipt(UUID merchantId, String receipt);

    Optional<OrderRecord> findByOrderIdAndMerchantId(UUID orderId, UUID merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderRecord o where o.orderId = :uuid and o.merchantId = :merchantId")
    Optional<OrderRecord> findByOrderIdAndMerchantIdForUpdate(UUID uuid, UUID merchantId);
}
