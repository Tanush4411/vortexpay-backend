package com.chrizlove.vortexpay.merchant.service.implementations;

import com.chrizlove.vortexpay.common.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.common.utils.RandomizerUtil;
import com.chrizlove.vortexpay.merchant.dto.Request.CreateApiKeyRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyCreateResponse;
import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyResponse;
import com.chrizlove.vortexpay.merchant.entity.ApiKey;
import com.chrizlove.vortexpay.merchant.entity.Merchant;
import com.chrizlove.vortexpay.merchant.mapper.ApiKeyMapper;
import com.chrizlove.vortexpay.merchant.repository.ApiKeyRepository;
import com.chrizlove.vortexpay.merchant.repository.MerchantRepository;
import com.chrizlove.vortexpay.merchant.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ApiKeyServiceImpl implements ApiKeyService {

    // TODO: Do caching

    private final MerchantRepository merchantRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request) {
        //Checking if merchant exists
        Merchant merchant = merchantRepository.findById(merchantId).
                orElseThrow(()-> new ResourceNotFoundException("merchant",merchantId));

        //key and secret generation
        String keyId= "rzp_"+request.apiEnvironment().name().toLowerCase()+"_"+ RandomizerUtil.randomBase64(24);
        String rawSecret=RandomizerUtil.randomBase64(40);

        //Generating apiKey
        ApiKey apiKey= ApiKey.builder().
                merchant(merchant).
                keyId(keyId).
                apiEnvironment(request.apiEnvironment()).
                keySecretHash(BCRYPT.encode(rawSecret)).
                build();
        apiKey=apiKeyRepository.save(apiKey);
        return new ApiKeyCreateResponse(apiKey.getId(),keyId, rawSecret, request.apiEnvironment());
    }

    @Override
    public List<ApiKeyResponse> listByMerchant(UUID merchantId) {
        return apiKeyMapper.toApiKeyResponses(apiKeyRepository.findByMerchant_Id(merchantId));
    }

    @Override
    @Transactional
    public void revoke(UUID merchantId, String keyId) {
        //Checking if api key exists
        ApiKey apiKey= apiKeyRepository.findByKeyId(keyId).
                filter(key -> key.getMerchant().getId().equals(merchantId)).
                orElseThrow(()-> new ResourceNotFoundException("ApiKey",keyId));
        apiKey.setEnabled(false);
        // TODO: Evict apiKey cache
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional
    public ApiKeyCreateResponse rotateKey(UUID merchantId, String keyId) {
        //Checking if api key exists
        ApiKey apiKey= apiKeyRepository.findByKeyId(keyId).
                filter(key -> key.getMerchant().getId().equals(merchantId)).
                orElseThrow(()-> new ResourceNotFoundException("ApiKey",keyId));

        //Checking if key is enabled
        if(!apiKey.isEnabled()) throw new RuntimeException("Cannot rotate a disabled ApiKey");

        //Key rotation logic
        String newRawSecret=RandomizerUtil.randomBase64(40);
        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(BCRYPT.encode(newRawSecret));
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodExpiresAt(LocalDateTime.now().plusHours(24));
        apiKey = apiKeyRepository.save(apiKey);
        // TODO: Evict apiKey cache
        return new ApiKeyCreateResponse(apiKey.getId(), apiKey.getKeyId(), apiKey.getKeySecretHash(), apiKey.getApiEnvironment());
    }
}
