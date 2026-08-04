package com.chrizlove.vortexpay.payment.gateway.adapter;

import com.chrizlove.vortexpay.payment.gateway.PaymentAdapter;
import com.chrizlove.vortexpay.payment.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment.gateway.dto.PaymentResult;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    //private final VaultService vaultService;

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
//        String token=(String)paymentRequest.methodDetails().get("token");
//        PaymentProcessorResponse paymentProcessorResponse=vaultService.charge(paymentRequest.paymentId(),token,paymentRequest.amount(),paymentRequest.methodDetails());
//        return switch (paymentProcessorResponse){
//            case PaymentProcessorResponse.Success success-> new PaymentResult.Success(success.bankReference());
//            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
//            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
//        };
        return new PaymentResult.Pending("def");
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");
    }
}
