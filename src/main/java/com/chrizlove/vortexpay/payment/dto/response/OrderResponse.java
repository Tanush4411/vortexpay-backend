package com.chrizlove.vortexpay.payment.dto.response;

import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.common.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record OrderResponse(

        UUID id,
        UUID merchantId,
        UUID customerId,
        String receipt,
        Money amount,
        OrderStatus status,
        Integer attempts,
        Map<String, Object> notes,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
