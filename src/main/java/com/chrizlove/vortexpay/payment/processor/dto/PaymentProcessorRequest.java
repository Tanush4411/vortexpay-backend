package com.chrizlove.vortexpay.payment.processor.dto;

import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequest(
        UUID processingId,
        UUID paymentId,
        PaymentMethod paymentMethod,
        Money amount,
        String pan,
        String expiry,
        Map<String, Object> methodDetails
) {

    public static PaymentProcessorRequest card(UUID paymentId,String pan, String expiry,Money amount, Map<String, Object> details) {
    return new PaymentProcessorRequest(UUID.randomUUID(),paymentId, PaymentMethod.CARD, amount, pan, expiry, details);
    }

    public static PaymentProcessorRequest nonCard(UUID paymentId, PaymentMethod method, Money amount, Map<String, Object> details) {
        return new PaymentProcessorRequest(UUID.randomUUID(),paymentId, method, amount, null,null, details);
    }
}
