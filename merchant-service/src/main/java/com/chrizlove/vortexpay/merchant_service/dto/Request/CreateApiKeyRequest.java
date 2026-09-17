package com.chrizlove.vortexpay.merchant_service.dto.Request;


import com.chrizlove.vortexpay.common_lib.enums.ApiEnvironment;

public record CreateApiKeyRequest(
        ApiEnvironment apiEnvironment
){
}
