package com.chrizlove.vortexpay.payment_service.processor;


import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
