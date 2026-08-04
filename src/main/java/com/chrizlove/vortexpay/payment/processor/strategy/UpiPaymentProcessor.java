package com.chrizlove.vortexpay.payment.processor.strategy;

import com.chrizlove.vortexpay.common.utils.RandomizerUtil;
import com.chrizlove.vortexpay.payment.processor.PaymentProcessor;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpiPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL="fail@okaxis";

        String bankCode=request.methodDetails()!=null?
                request.methodDetails().get("vpa").toString():null;

        //simulation
        if(VPA_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "Bank rejected the transaction registration");
        }

        String processorRef= "UPI_PROCESSOR_"+ RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
