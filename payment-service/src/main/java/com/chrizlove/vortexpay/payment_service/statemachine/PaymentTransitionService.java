package com.chrizlove.vortexpay.payment_service.statemachine;

import com.chrizlove.vortexpay.common_lib.context.MerchantContext;
import com.chrizlove.vortexpay.common_lib.enums.PaymentActor;
import com.chrizlove.vortexpay.common_lib.enums.PaymentEvent;
import com.chrizlove.vortexpay.common_lib.enums.PaymentStatus;
import com.chrizlove.vortexpay.payment_service.entity.Payment;
import com.chrizlove.vortexpay.payment_service.entity.PaymentTransitionLog;
import com.chrizlove.vortexpay.payment_service.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final MerchantContext  merchantContext;

    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent){
        PaymentStatus next=paymentStateMachine.transition(payment.getPaymentStatus(),paymentEvent);

        PaymentActor paymentActor= getPaymentActor();

        PaymentTransitionLog log=PaymentTransitionLog.builder().
                payment(payment).
                paymentEvent(paymentEvent).
                fromStatus(payment.getPaymentStatus()).
                toStatus(next).
                actor(paymentActor).
                occurredAt(LocalDateTime.now())
                .build();
        payment.setPaymentStatus(next);
        paymentTransitionLogRepository.save(log);
        return next;
    }

    private PaymentActor getPaymentActor() {
        try {
            String keyId = merchantContext.getKeyId();
            UUID merchantId = merchantContext.getMerchantId();

            if (keyId != null && !keyId.isBlank()) {
                return PaymentActor.CUSTOMER;
            } else if (merchantId != null) {
                return PaymentActor.MERCHANT;
            }
        } catch (Exception ignored) {
        }
        return PaymentActor.SYSTEM;
    }
}
