package com.chrizlove.vortexpay.payment.gateway.adapter;

import com.chrizlove.vortexpay.common.enums.PaymentMethod;
import com.chrizlove.vortexpay.payment.gateway.PaymentAdapter;
import com.chrizlove.vortexpay.payment.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment.gateway.dto.PaymentResult;
import com.chrizlove.vortexpay.payment.processor.PaymentProcessorRouter;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorRequest;
import com.chrizlove.vortexpay.payment.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpiPaymentAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        log.info("initiate paymentRequest with UpiBankingAdapter, paymentId:{}", paymentRequest.paymentId());
        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.
                    nonCard(paymentRequest.paymentId(),
                            PaymentMethod.UPI,
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
            log.warn("UPI failed, paymentId:{}", paymentRequest.paymentId());
            return new PaymentResult.Failure("UPI_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("UPI_REF");
    }
}
