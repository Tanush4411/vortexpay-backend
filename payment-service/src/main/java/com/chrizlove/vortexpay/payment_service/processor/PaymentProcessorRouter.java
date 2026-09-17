package com.chrizlove.vortexpay.payment_service.processor;

import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.common_lib.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        PaymentProcessor processor= paymentProcessorMap.get(request.paymentMethod());
        if(processor==null){
            throw new IllegalArgumentException("PaymentProcessor Not Found for payment method "+request.paymentMethod());
        }
        return processor.charge(request);
    }
}
