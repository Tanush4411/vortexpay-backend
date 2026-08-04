package com.chrizlove.vortexpay.merchant.dto.Request;


import com.chrizlove.vortexpay.common.enums.ApiEnvironment;

public record CreateApiKeyRequest(
        ApiEnvironment apiEnvironment
){
}
