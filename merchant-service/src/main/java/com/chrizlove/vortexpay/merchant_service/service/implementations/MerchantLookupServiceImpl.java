package com.chrizlove.vortexpay.merchant_service.service.implementations;

import com.chrizlove.vortexpay.common_lib.dto.SettlementBankDetails;
import com.chrizlove.vortexpay.common_lib.dto.WebhookTarget;
import com.chrizlove.vortexpay.common_lib.enums.MerchantStatus;
import com.chrizlove.vortexpay.common_lib.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.merchant_service.api.MerchantLookupService;
import com.chrizlove.vortexpay.merchant_service.entity.Merchant;
import com.chrizlove.vortexpay.merchant_service.repository.MerchantRepository;
import com.chrizlove.vortexpay.merchant_service.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MerchantLookupServiceImpl implements MerchantLookupService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor bytesEncryptor;

    @Override
    public List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
                .filter(webhookConfig -> webhookConfig.isSubscribedTo(eventType))
                .map(webhookConfig -> {
                    byte[] cipherBytes = Base64.getDecoder().decode(webhookConfig.getWebhookSecret());
                    byte[] decryptedSecretBytes = bytesEncryptor.decrypt(cipherBytes);
                    return new WebhookTarget(webhookConfig.getId(), webhookConfig.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                }).toList();
    }

    @Override
    public List<UUID> listActiveMerchantIds() {
        return merchantRepository.findByMerchantStatus(MerchantStatus.ACTIVE)
                .stream().map(m->m.getId()).toList();
    }

    @Override
    public SettlementBankDetails getSettlementBankDetails(UUID merchantId) {
        Merchant merchant= merchantRepository.findById(merchantId).
                orElseThrow(()->new ResourceNotFoundException("merchant", merchantId));

        return new SettlementBankDetails(
                merchant.getSettlementBankAccount(),
                merchant.getSettlementBankIFSC(),
                merchant.getSettlementBankAccountHolderName()
        );
    }
}
