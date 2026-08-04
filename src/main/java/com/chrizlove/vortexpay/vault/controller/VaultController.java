package com.chrizlove.vortexpay.vault.controller;

import com.chrizlove.vortexpay.merchant.security.MerchantContext;
import com.chrizlove.vortexpay.vault.dto.request.TokenizeRequest;
import com.chrizlove.vortexpay.vault.dto.response.TokenizeResponse;
import com.chrizlove.vortexpay.vault.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final VaultService vaultService;
    private final MerchantContext merchantContext;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody @Valid TokenizeRequest tokenizeRequest){
     return ResponseEntity.status(HttpStatus.CREATED).body(vaultService.tokenize(tokenizeRequest,merchantContext.getMerchantId()));
    }
}
