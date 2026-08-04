package com.chrizlove.vortexpay.payment.processor.strategy;

import com.chrizlove.vortexpay.common.utils.RandomizerUtil;
import com.chrizlove.vortexpay.payment.processor.PaymentProcessor;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor implements PaymentProcessor {

    public static final String PAN_CARD_DECLINE="4000000000000002";
    public static final String PAN_CARD_EXPIRED="4000000000000069";

    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        String pan=request.pan();

        if(PAN_CARD_DECLINE.equals(pan)){
            log.warn("Card declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED","Card declined by bank");

        }

        if(PAN_CARD_EXPIRED.equals(pan)){
            log.warn("Card expired");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED","Card expired by bank");

        }

        String processorRef= "CARD_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
