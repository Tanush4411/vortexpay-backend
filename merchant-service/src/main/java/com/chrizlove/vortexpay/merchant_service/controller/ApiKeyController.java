package com.chrizlove.vortexpay.merchant_service.controller;

import com.chrizlove.vortexpay.common_lib.context.MerchantContext;
import com.chrizlove.vortexpay.merchant_service.dto.Request.CreateApiKeyRequest;
import com.chrizlove.vortexpay.merchant_service.dto.Response.ApiKeyCreateResponse;
import com.chrizlove.vortexpay.merchant_service.dto.Response.ApiKeyResponse;
import com.chrizlove.vortexpay.merchant_service.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(apiKeyService.create(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listByMerchant(){
    return ResponseEntity.ok(apiKeyService.listByMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> revoke(@PathVariable String keyId) {
    apiKeyService.revoke(merchantContext.getMerchantId(),keyId);
    return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotateKey(@PathVariable String keyId) {
        return ResponseEntity.ok(apiKeyService.rotateKey(merchantContext.getMerchantId(),keyId));
    }

}
