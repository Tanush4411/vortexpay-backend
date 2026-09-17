package com.chrizlove.vortexpay.merchant_service.dto.Response;


import com.chrizlove.vortexpay.common_lib.enums.ApiEnvironment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponse(
        UUID id,
        String keyId,
        ApiEnvironment apiEnvironment,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt
){
}
