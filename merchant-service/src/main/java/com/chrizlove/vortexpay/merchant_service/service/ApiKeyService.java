package com.chrizlove.vortexpay.merchant_service.service;


import com.chrizlove.vortexpay.merchant_service.dto.Request.CreateApiKeyRequest;
import com.chrizlove.vortexpay.merchant_service.dto.Response.ApiKeyCreateResponse;
import com.chrizlove.vortexpay.merchant_service.dto.Response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
     ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);

     List<ApiKeyResponse> listByMerchant(UUID merchantId);

     void revoke(UUID merchantId, String keyId);

    ApiKeyCreateResponse rotateKey(UUID merchantId, String keyId);
}
