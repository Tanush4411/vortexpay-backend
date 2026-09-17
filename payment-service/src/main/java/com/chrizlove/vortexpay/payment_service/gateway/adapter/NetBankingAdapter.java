package com.chrizlove.vortexpay.payment_service.gateway.adapter;

import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.common_lib.dto.PaymentProcessorResponse;
import com.chrizlove.vortexpay.common_lib.enums.PaymentMethod;
import com.chrizlove.vortexpay.payment_service.gateway.PaymentAdapter;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentResult;
import com.chrizlove.vortexpay.payment_service.processor.PaymentProcessorRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        log.info("initiate paymentRequest with NetBankingAdapter, paymentId:{}", paymentRequest.paymentId());
        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.
                    nonCard(paymentRequest.paymentId(),
                            PaymentMethod.NETBANKING,
                            paymentRequest.amount(),
                            paymentRequest.methodDetails()
                    );

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);
            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Success success ->
                        new PaymentResult.Success(success.bankReference());

            };
        }catch (Exception e){
            log.warn("NetBanking failed, paymentId:{}", paymentRequest.paymentId());
            return new PaymentResult.Failure("NBK_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success( "NBK_REF");
    }
}
