package com.chrizlove.vortexpay.payment_service.api;

import com.chrizlove.vortexpay.common_lib.dto.PaymentSettlementView;
import com.chrizlove.vortexpay.payment_service.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentLookupService {

    List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId);

    void markSettled(List<UUID> paymentList);
}
