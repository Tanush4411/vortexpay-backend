package com.chrizlove.vortexpay.payment.statemachine;

import com.chrizlove.vortexpay.common.enums.PaymentActor;
import com.chrizlove.vortexpay.common.enums.PaymentEvent;
import com.chrizlove.vortexpay.common.enums.PaymentStatus;
import com.chrizlove.vortexpay.payment.entity.Payment;
import com.chrizlove.vortexpay.payment.entity.PaymentTransitionLog;
import com.chrizlove.vortexpay.payment.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent){
        PaymentStatus next=paymentStateMachine.transition(payment.getPaymentStatus(),paymentEvent);
        PaymentTransitionLog log=PaymentTransitionLog.builder().
                payment(payment).
                paymentEvent(paymentEvent).
                fromStatus(payment.getPaymentStatus()).
                toStatus(next).
                actor(PaymentActor.SYSTEM).
                occurredAt(LocalDateTime.now())
                .build();
        payment.setPaymentStatus(next);
        paymentTransitionLogRepository.save(log);
        return next;
    }
}
