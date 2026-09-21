package com.chrizlove.vortexpay.payment_service.service.impl;

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
import com.chrizlove.vortexpay.payment_service.gateway.PaymentGatewayRouter;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment_service.gateway.dto.PaymentResult;
import com.chrizlove.vortexpay.payment_service.mapper.PaymentMapper;
import com.chrizlove.vortexpay.payment_service.outbox.OutboxEventPublisher;
import com.chrizlove.vortexpay.payment_service.repository.OrderRepository;
import com.chrizlove.vortexpay.payment_service.repository.PaymentRepository;
import com.chrizlove.vortexpay.payment_service.saga.PaymentAuthorizationRecorder;
import com.chrizlove.vortexpay.payment_service.service.PaymentService;
import com.chrizlove.vortexpay.payment_service.statemachine.PaymentTransitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentAuthorizationRecorder  paymentAuthorizationRecorder;

    @Override
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {

        Payment payment = paymentAuthorizationRecorder.recordPayment(merchantId, request);

        PaymentRequest paymentRequest=new PaymentRequest(payment.getId(),request.orderId(),merchantId,payment.getAmount(),request.paymentMethod(),request.methodDetails());

        PaymentResult result;
        try{
            result= paymentGatewayRouter.initiate(paymentRequest);
        } catch (Exception e) {
            return paymentAuthorizationRecorder.compensateAuthorizationFailure(payment.getId(),
                    "PAYMENT_GATEWAY_ROUTER_UNREACHABLE", e.getMessage());
        }

        return paymentAuthorizationRecorder.applyGatewayResult(payment.getId(), result);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {

        //pessimistic locking
        Payment payment=paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId).
                orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult= paymentGatewayRouter.capture(payment.getPaymentMethod(),paymentId);

        if(paymentResult instanceof PaymentResult.Success success){
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            log.info("Payment captured, paymentId: {}",paymentId);
        }
        else if(paymentResult instanceof PaymentResult.Failure failure){
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.info("Payment capture failed, paymentId: {}",paymentId);
        }
        payment= paymentRepository.save(payment);

        //pushing the kafka event to our db
        eventPublisher.publish(EventAggregateType.PAYMENT,payment.getId(),"PAYMENT_STATUS_CHANGED",
                Map.of("orderId",payment.getOrderRecord().getOrderId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId",payment.getMerchantId().toString(),
                        "paymentStatus", payment.getPaymentStatus().name(),
                        "amountUnits",payment.getAmount().getAmountUnits(),
                        "amountCurrency",payment.getAmount().getCurrency(),
                        "method", payment.getPaymentMethod().name())
        );

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription) {

        //pessimistic locking
        Payment payment=paymentRepository.findByIdForUpdate(paymentId).
                orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if(payment.getPaymentStatus()!=PaymentStatus.AUTHORIZING){
            log.warn("Payment not in AUTHORIZING state, paymentId: {}, paymentStatus: {}",paymentId, payment.getPaymentStatus());
            return;
        }
        OrderRecord order=payment.getOrderRecord();

        if(approve){
            paymentTransitionService.apply(payment,PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            //Auto-Capture
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult=paymentGatewayRouter.capture(payment.getPaymentMethod(),paymentId);

            if(captureResult instanceof PaymentResult.Success success){
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.PAID);
            }
            else if(captureResult instanceof PaymentResult.Failure failure){
            paymentTransitionService.apply(payment,PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            }
        }
        else{
            paymentTransitionService.apply(payment,PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        //pushing the kafka event to our db
        eventPublisher.publish(EventAggregateType.PAYMENT,payment.getId(),"PAYMENT_STATUS_CHANGED",
                Map.of("orderId",payment.getOrderRecord().getOrderId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId",payment.getMerchantId().toString(),
                        "paymentStatus", payment.getPaymentStatus().name(),
                        "amountUnits",payment.getAmount().getAmountUnits(),
                        "amountCurrency",payment.getAmount().getCurrency(),
                        "method", payment.getPaymentMethod())
        );
    }
}
