package com.chrizlove.vortexpay.common_lib.dto;

public sealed interface PaymentProcessorResponse permits PaymentProcessorResponse.Failure, PaymentProcessorResponse.Pending, PaymentProcessorResponse.Success{

    record Pending(String processorReference) implements  PaymentProcessorResponse {}

    record Success(String processorReference, String bankReference) implements  PaymentProcessorResponse{}

    record Failure(String errorCode, String errorDescription) implements  PaymentProcessorResponse{}
}
