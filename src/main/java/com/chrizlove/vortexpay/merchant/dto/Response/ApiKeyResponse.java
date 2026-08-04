package com.chrizlove.vortexpay.merchant.dto.Response;


import com.chrizlove.vortexpay.common.enums.ApiEnvironment;

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
