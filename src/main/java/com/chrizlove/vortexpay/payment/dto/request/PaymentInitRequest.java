package com.chrizlove.vortexpay.payment.dto.request;

import com.chrizlove.vortexpay.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(

        @NotNull(message = "Order id is required")
        UUID orderId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        Map<String, Object> methodDetails
) {
}
