package com.chrizlove.vortexpay.vault.service;

import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.vault.dto.request.TokenizeRequest;
import com.chrizlove.vortexpay.vault.dto.response.TokenizeResponse;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
     TokenizeResponse tokenize(@Valid TokenizeRequest tokenizeRequest, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
