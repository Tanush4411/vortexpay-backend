package com.chrizlove.vortexpay.payment.service.impl;

import com.chrizlove.vortexpay.common.enums.PaymentStatus;
import com.chrizlove.vortexpay.payment.api.PaymentLookupService;
import com.chrizlove.vortexpay.payment.entity.Payment;
import com.chrizlove.vortexpay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> findUnsettledCapturedPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdAndPaymentStatusForUpdate(merchantId, PaymentStatus.CAPTURED);
    }
}
