package com.chrizlove.vortexpay.vault_service.processor;

import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.common_lib.utils.RandomizerUtil;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.concurrent.CompletedFuture;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class CardPaymentProcessor {

    public static final String PAN_CARD_DECLINE="4000000000000002";
    public static final String PAN_CARD_EXPIRED="4000000000000069";

    @Bulkhead(name="vault-card-processor", type = Bulkhead.Type.THREADPOOL)
    public CompletableFuture<PaymentProcessorResponse> charge(PaymentProcessorRequest request) {

        String pan=request.pan();

        if(PAN_CARD_DECLINE.equals(pan)){
            log.warn("Card declined");
            return CompletableFuture.completedFuture(new PaymentProcessorResponse.Failure("CARD_DECLINED","Card declined by bank"));

        }

        if(PAN_CARD_EXPIRED.equals(pan)){
            log.warn("Card expired");
            return CompletableFuture.completedFuture(new PaymentProcessorResponse.Failure("CARD_EXPIRED","Card expired by bank"));

        }

        String processorRef= "CARD_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return CompletableFuture.completedFuture(new PaymentProcessorResponse.Pending(processorRef));
    }
}
