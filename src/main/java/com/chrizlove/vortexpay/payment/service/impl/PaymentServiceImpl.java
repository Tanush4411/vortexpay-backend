package com.chrizlove.vortexpay.payment.service.impl;

import com.chrizlove.vortexpay.common.enums.EventAggregateType;
import com.chrizlove.vortexpay.common.enums.OrderStatus;
import com.chrizlove.vortexpay.common.enums.PaymentEvent;
import com.chrizlove.vortexpay.common.enums.PaymentStatus;
import com.chrizlove.vortexpay.common.exceptions.BusinessRuleViolationException;
import com.chrizlove.vortexpay.common.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.payment.dto.request.PaymentInitRequest;
import com.chrizlove.vortexpay.payment.dto.response.PaymentResponse;
import com.chrizlove.vortexpay.payment.entity.OrderRecord;
import com.chrizlove.vortexpay.payment.entity.Payment;
import com.chrizlove.vortexpay.payment.gateway.PaymentGatewayRouter;
import com.chrizlove.vortexpay.payment.gateway.dto.PaymentRequest;
import com.chrizlove.vortexpay.payment.gateway.dto.PaymentResult;
import com.chrizlove.vortexpay.payment.mapper.PaymentMapper;
import com.chrizlove.vortexpay.payment.repository.OrderRepository;
import com.chrizlove.vortexpay.payment.repository.PaymentRepository;
import com.chrizlove.vortexpay.payment.service.PaymentService;
import com.chrizlove.vortexpay.payment.statemachine.PaymentTransitionService;
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
                //payment.setPaymentStatus(PaymentStatus.FAILED);
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

        //TODO: Send kafka outbox event to db

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {

//        Payment payment=paymentRepository.findByIdAndMerchantId(paymentId, merchantId).
//                orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

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

        //TODO: Send kafka outbox event to db

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription) {

//        Payment payment=paymentRepository.findById(paymentId).
//                orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

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

        //TODO: Send kafka outbox event to db
    }
}
