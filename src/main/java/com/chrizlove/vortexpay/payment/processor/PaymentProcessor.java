package com.chrizlove.vortexpay.payment.processor;


import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
