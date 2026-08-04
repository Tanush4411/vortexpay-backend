package com.chrizlove.vortexpay.merchant.service;



import com.chrizlove.vortexpay.merchant.dto.Request.CreateApiKeyRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyCreateResponse;
import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
     ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);

     List<ApiKeyResponse> listByMerchant(UUID merchantId);

     void revoke(UUID merchantId, String keyId);

    ApiKeyCreateResponse rotateKey(UUID merchantId, String keyId);
}
