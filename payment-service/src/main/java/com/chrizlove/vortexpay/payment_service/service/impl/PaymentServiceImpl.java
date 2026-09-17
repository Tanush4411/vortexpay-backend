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

    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {

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
                idempotencyKey(UUID.randomUUID().toString()).
                paymentStatus(PaymentStatus.CREATED).
                paymentMethod(request.paymentMethod()).
                methodDetails(request.methodDetails()).
                build();

        payment= paymentRepository.save(payment);

        PaymentRequest paymentRequest=new PaymentRequest(payment.getId(),request.orderId(),merchantId,payment.getAmount(),request.paymentMethod(),request.methodDetails());

        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        PaymentResult result= paymentGatewayRouter.initiate(paymentRequest);

        switch (result){
            case PaymentResult.Pending pending-> payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure-> {
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success -> {
                log.warn("Invalid state");
                return null;
            }
        }
        payment= paymentRepository.save(payment);
        orderRepository.save(order);

        //pushing the kafka event to our db
        eventPublisher.publish(EventAggregateType.PAYMENT,payment.getId(),"PAYMENT_CREATED",
                Map.of("orderId",order.getOrderId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId",order.getMerchantId().toString(),
                        "paymentStatus", payment.getPaymentStatus().name(),
                        "amountUnits",order.getAmount().getAmountUnits(),
                        "amountCurrency",order.getAmount().getCurrency(),
                        "method", payment.getPaymentMethod().name())
        );

        return paymentMapper.toResponse(payment);
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
