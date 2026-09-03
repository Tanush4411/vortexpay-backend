package com.chrizlove.vortexpay.merchant.service.implementations;

import com.chrizlove.vortexpay.common.dto.WebhookTarget;
import com.chrizlove.vortexpay.common.enums.MerchantStatus;
import com.chrizlove.vortexpay.common.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.common.utils.RandomizerUtil;
import com.chrizlove.vortexpay.merchant.api.MerchantLookupService;
import com.chrizlove.vortexpay.merchant.dto.Request.UpdateWebhookConfigRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.WebhookConfigResponse;
import com.chrizlove.vortexpay.merchant.entity.Merchant;
import com.chrizlove.vortexpay.merchant.entity.MerchantWebhookConfig;
import com.chrizlove.vortexpay.merchant.mapper.WebhookConfigMapper;
import com.chrizlove.vortexpay.merchant.repository.MerchantRepository;
import com.chrizlove.vortexpay.merchant.repository.WebhookConfigRepository;
import com.chrizlove.vortexpay.merchant.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService, MerchantLookupService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor bytesEncryptor;
    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    public WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request) {
        //Merchant Existence check
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        String rawSecret= RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes= rawSecret.getBytes(StandardCharsets.UTF_8);
        String encryptedSecret= Base64.getEncoder().encodeToString(bytesEncryptor.encrypt(rawSecretBytes));

        MerchantWebhookConfig config=MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .enabled(true)
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();

        config=webhookConfigRepository.save(config);

        return webhookConfigMapper.toResponse(config,rawSecret);
    }

    @Override
    public List<WebhookConfigResponse> list(UUID merchantId) {
        return webhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponse(config, null))
                .toList();
    }

    @Override
    public WebhookConfigResponse getById(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateWebhookConfigRequest request) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        config.setTargetUrl(request.targetUrl());
        config.setEventTypes(request.eventTypes());
        log.info("Merchant webhook config updated id={} merchantId={}", configId, merchantId);
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        webhookConfigRepository.delete(config);
        log.info("Merchant webhook config deleted id={} merchantId={}", configId, merchantId);
    }

    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID configId) {
        return webhookConfigRepository.findByIdAndMerchant_Id(configId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("MerchantWebhookConfig", configId));
    }

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
}
