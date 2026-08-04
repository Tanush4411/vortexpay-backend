package com.chrizlove.vortexpay.merchant.controller;

import com.chrizlove.vortexpay.merchant.dto.Request.CreateApiKeyRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyCreateResponse;
import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyResponse;
import com.chrizlove.vortexpay.merchant.security.MerchantContext;
import com.chrizlove.vortexpay.merchant.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
