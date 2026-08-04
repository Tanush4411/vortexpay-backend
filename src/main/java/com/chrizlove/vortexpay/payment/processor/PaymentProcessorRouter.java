package com.chrizlove.vortexpay.payment.processor;

import com.chrizlove.vortexpay.common.enums.PaymentMethod;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
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
