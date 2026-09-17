package com.chrizlove.vortexpay.payment_service.service.impl;

import com.chrizlove.vortexpay.common_lib.dto.PaymentSettlementView;
import com.chrizlove.vortexpay.common_lib.enums.PaymentStatus;
import com.chrizlove.vortexpay.payment_service.api.PaymentLookupService;
import com.chrizlove.vortexpay.payment_service.entity.Payment;
import com.chrizlove.vortexpay.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId) {
        List<Payment> paymentList= paymentRepository.findByMerchantIdAndPaymentStatusForUpdate(merchantId, PaymentStatus.CAPTURED);

        return paymentList.stream()
                .map(p-> new PaymentSettlementView(
                        p.getId(),p.getAmount().getAmountUnits(),0,p.getAmount().getCurrency()
                )).toList();
    }

    @Override
    @Transactional
    public void markSettled(List<UUID> paymentList) {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> payments = paymentRepository.findAllById(paymentList);
        for (Payment payment : payments) {
            payment.setPaymentStatus(PaymentStatus.SETTLED);
            payment.setSettledAt(now);
        }
        paymentRepository.saveAll(payments);
    }
}
