package com.chrizlove.vortexpay.payment.processor.strategy;

import com.chrizlove.vortexpay.common.utils.RandomizerUtil;
import com.chrizlove.vortexpay.payment.processor.PaymentProcessor;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NetBankingPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String BANK_CODE_FAIL="BANK_CODE_FAIL";

        String bankCode=request.methodDetails()!=null?
                request.methodDetails().get("bank").toString():null;

        //simulation
        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("BANK_REJECTED",
                    "Bank rejected the transaction registration");
        }

        String processorRef= "NBK_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
