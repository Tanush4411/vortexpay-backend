package com.chrizlove.vortexpay.payment_service.gateway.adapter;

import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.common_lib.dto.VaultChargeRequest;
import com.chrizlove.vortexpay.payment_service.client.VaultServiceClient;
import com.chrizlove.vortexpay.payment_service.gateway.PaymentAdapter;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        String token=(String)paymentRequest.methodDetails().get("token");
        PaymentProcessorResponse paymentProcessorResponse=vaultServiceClient
                .charge(new VaultChargeRequest(paymentRequest.paymentId(),
                        token,paymentRequest.amount(), paymentRequest.methodDetails()));

        return switch (paymentProcessorResponse){
            case PaymentProcessorResponse.Success success-> new PaymentResult.Success(success.bankReference());
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");
    }
}
