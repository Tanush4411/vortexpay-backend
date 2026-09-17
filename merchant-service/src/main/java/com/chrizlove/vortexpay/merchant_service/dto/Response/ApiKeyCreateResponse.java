package com.chrizlove.vortexpay.merchant_service.dto.Response;

import com.chrizlove.vortexpay.common_lib.enums.ApiEnvironment;

import java.util.UUID;

public record ApiKeyCreateResponse (
        UUID id,
        String keyId,
        String keySecret,
        ApiEnvironment apiEnvironment
){
}
