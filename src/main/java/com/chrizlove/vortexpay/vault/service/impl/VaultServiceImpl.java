package com.chrizlove.vortexpay.vault.service.impl;

import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.common.enums.CardBrand;
import com.chrizlove.vortexpay.common.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.common.utils.RandomizerUtil;
import com.chrizlove.vortexpay.payment.processor.PaymentProcessorRouter;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.vault.config.VaultEncryptionConfig;
import com.chrizlove.vortexpay.vault.dto.request.TokenizeRequest;
import com.chrizlove.vortexpay.vault.dto.response.TokenizeResponse;
import com.chrizlove.vortexpay.vault.entity.CardToken;
import com.chrizlove.vortexpay.vault.entity.VaultCard;
import com.chrizlove.vortexpay.vault.repository.CardTokenRepository;
import com.chrizlove.vortexpay.vault.repository.VaultCardRepository;
import com.chrizlove.vortexpay.vault.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest tokenizeRequest, UUID merchantId) {
        log.info("tokenizeRequest:{}",tokenizeRequest);
        String lastFour=tokenizeRequest.pan().substring(tokenizeRequest.pan().length()-4);
        String bin=tokenizeRequest.pan().substring(0,6);
        CardBrand cardBrand=detectBrand(tokenizeRequest.pan());

        byte[] dek=KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan= VaultEncryptionConfig.panEncryptor(dek).
                encrypt(tokenizeRequest.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek=dekEncryptor.encrypt(dek);

        VaultCard vaultCard=VaultCard.builder().
                brand(cardBrand).
                expiryMonth(tokenizeRequest.expiryMonth().toString()).
                expiryYear(tokenizeRequest.expiryYear().toString()).
                bin(bin).
                lastFourDigits(lastFour).
                encryptedPan(encryptedPan).
                cardHolderName(tokenizeRequest.cardHolderName()).
                encryptedDek(encryptedDek).
                build();

        vaultCard=vaultCardRepository.save(vaultCard);
        String token="tok_"+ RandomizerUtil.randomBase64(32);

        CardToken cardToken= CardToken.builder().
                token(token).
                vaultCard(vaultCard).
                customerId(tokenizeRequest.customerId()).
                merchantId(merchantId).
                build();
        cardTokenRepository.save(cardToken);
        return new TokenizeResponse(token,lastFour,cardBrand, tokenizeRequest.expiryMonth(), tokenizeRequest.expiryYear());
    }

    @Override
    @Transactional
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails) {
        CardToken cardToken= cardTokenRepository.findByTokenAndRevokedAtIsNull(token).
                orElseThrow(()-> new ResourceNotFoundException("CardToken", token));
        VaultCard vaultCard=cardToken.getVaultCard();
        byte[] panBytes=null;

        try {
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());
            panBytes = VaultEncryptionConfig.panEncryptor(dek).decrypt(vaultCard.getEncryptedPan());
            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.
                    card(paymentId, pan, expiry, amount, methodDetails);

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.
                    charge(paymentProcessorRequest);

            log.info("Vault charge registered, token={}*****", token.substring(0, 4));
            return paymentProcessorResponse;
        }catch (Exception e){
            log.warn("Vault charge failed, token={}*****", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED",e.getMessage());
        } finally {
            if(panBytes!=null) Arrays.fill(panBytes, (byte) 0);
        }
    }

    private CardBrand detectBrand(String pan) {
        if(pan.startsWith("4")) return CardBrand.VISA;
        if(pan.startsWith("5") || pan.startsWith("2")) return CardBrand.MASTERCARD;
        if(pan.startsWith("37") || pan.startsWith("34")) return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
