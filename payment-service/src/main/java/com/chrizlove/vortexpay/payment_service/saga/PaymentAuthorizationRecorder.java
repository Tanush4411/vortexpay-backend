package com.chrizlove.vortexpay.payment_service.saga;

import com.chrizlove.vortexpay.common_lib.enums.EventAggregateType;
import com.chrizlove.vortexpay.common_lib.enums.OrderStatus;
import com.chrizlove.vortexpay.common_lib.enums.PaymentEvent;
import com.chrizlove.vortexpay.common_lib.enums.PaymentStatus;
import com.chrizlove.vortexpay.common_lib.exceptions.BusinessRuleViolationException;
import com.chrizlove.vortexpay.common_lib.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.payment_service.dto.request.PaymentInitRequest;
import com.chrizlove.vortexpay.payment_service.dto.response.PaymentResponse;
import com.chrizlove.vortexpay.payment_service.entity.OrderRecord;
import com.chrizlove.vortexpay.payment_service.entity.Payment;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentResult;
import com.chrizlove.vortexpay.payment_service.mapper.PaymentMapper;
import com.chrizlove.vortexpay.payment_service.outbox.OutboxEventPublisher;
import com.chrizlove.vortexpay.payment_service.repository.OrderRepository;
import com.chrizlove.vortexpay.payment_service.repository.PaymentRepository;
import com.chrizlove.vortexpay.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentAuthorizationRecorder {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransitionService paymentTransitionService;
    private final PaymentMapper paymentMapper;
    private final OutboxEventPublisher eventPublisher;

    @Transactional
    public Payment recordPayment(UUID merchantId, PaymentInitRequest request, String idempotencyKey) {

        //does pessimistic locking so that until a thread is done executing, another thread cant even
        //read the data
        OrderRecord order= orderRepository.findByOrderIdAndMerchantIdForUpdate(request.orderId(), merchantId).
                orElseThrow(() -> new ResourceNotFoundException("Order", request.orderId()));

        if(order.getOrderStatus()!= OrderStatus.CREATED && order.getOrderStatus()!= OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE","Order cannot accept payment in this status: "+order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts() + 1);

        Payment payment=Payment.builder().
                orderRecord(order).
                merchantId(merchantId).
                amount(order.getAmount()).
                idempotencyKey(idempotencyKey!=null?idempotencyKey: UUID.randomUUID().toString()).
                paymentStatus(PaymentStatus.CREATED).
                paymentMethod(request.paymentMethod()).
                methodDetails(request.methodDetails()).
                build();

        payment= paymentRepository.save(payment);
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        return payment;
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> findExistingAttempt(UUID merchantId, String idempotencyKey) {
        return paymentRepository.findByMerchantIdAndIdempotencyKey(merchantId, idempotencyKey)
                .map(paymentMapper::toResponse);
    }

    @Transactional
    public PaymentResponse compensateAuthorizationFailure(UUID paymentId, String errorCode, String errorDesc) {

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDesc);
        payment = paymentRepository.save(payment);

        publishStatusEvent(payment, "PAYMENT_AUTHORIZATION_COMPENSATED");
        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse applyGatewayResult(UUID paymentId, PaymentResult result) {
        log.info("Applying Gateway result for paymentId: {}", paymentId);
        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        switch (result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure -> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success ->
                    log.warn("Invalid state: initiate() gateway call returned Success directly, paymentId={}", paymentId);
        }

        payment = paymentRepository.save(payment);
        publishStatusEvent(payment, "PAYMENT_CREATED");
        log.info("Successfully applied Gateway result for paymentId: {}", paymentId);
        return paymentMapper.toResponse(payment);
    }

    private void publishStatusEvent(Payment payment, String eventType) {
        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), eventType,
                Map.of("orderId", payment.getOrderRecord().getOrderId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getPaymentStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getPaymentMethod()
                ));
    }
}
