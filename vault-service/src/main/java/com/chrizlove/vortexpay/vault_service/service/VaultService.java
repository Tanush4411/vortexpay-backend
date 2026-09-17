package com.chrizlove.vortexpay.vault_service.service;

import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.common_lib.entity.Money;
import com.chrizlove.vortexpay.vault_service.dto.request.TokenizeRequest;
import com.chrizlove.vortexpay.vault_service.dto.response.TokenizeResponse;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
     TokenizeResponse tokenize(@Valid TokenizeRequest tokenizeRequest, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
