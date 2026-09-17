package com.chrizlove.vortexpay.payment_service.processor.strategy;

import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.common_lib.utils.RandomizerUtil;
import com.chrizlove.vortexpay.payment_service.processor.PaymentProcessor;
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
