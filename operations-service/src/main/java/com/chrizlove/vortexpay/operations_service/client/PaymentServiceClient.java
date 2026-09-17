package com.chrizlove.vortexpay.operations_service.client;

import com.chrizlove.vortexpay.common_lib.dto.PaymentSettlementView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "payment-service", path="/internal/payments")
public interface PaymentServiceClient {

    @GetMapping("/unsettled-captured")
    List<PaymentSettlementView> findUnsettledCapturedPayments(@RequestParam UUID merchantId);

    @PostMapping("/mark-settled")
    void markSettled(@RequestParam List<UUID> paymentIds);
}
