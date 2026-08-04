package com.chrizlove.vortexpay.payment.gateway.dto;

import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.common.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest (
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod paymentMethod,
        Map<String, Object> methodDetails
){
}
