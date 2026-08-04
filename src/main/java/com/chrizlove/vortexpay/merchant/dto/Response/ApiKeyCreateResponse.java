package com.chrizlove.vortexpay.merchant.dto.Response;


import com.chrizlove.vortexpay.common.enums.ApiEnvironment;

import java.util.UUID;

public record ApiKeyCreateResponse (
        UUID id,
        String keyId,
        String keySecret,
        ApiEnvironment apiEnvironment
){
}
