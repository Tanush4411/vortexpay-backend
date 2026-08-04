package com.chrizlove.vortexpay.payment.service.impl;

import com.chrizlove.vortexpay.common.enums.EventAggregateType;
import com.chrizlove.vortexpay.common.enums.OrderStatus;
import com.chrizlove.vortexpay.common.exceptions.BusinessRuleViolationException;
import com.chrizlove.vortexpay.common.exceptions.DuplicateResourceException;
import com.chrizlove.vortexpay.common.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.merchant.service.CustomerService;
import com.chrizlove.vortexpay.payment.dto.request.CreateOrderRequest;
import com.chrizlove.vortexpay.payment.dto.response.OrderResponse;
import com.chrizlove.vortexpay.payment.dto.response.PaymentResponse;
import com.chrizlove.vortexpay.payment.entity.OrderRecord;
import com.chrizlove.vortexpay.payment.entity.Payment;
import com.chrizlove.vortexpay.payment.mapper.OrderMapper;
import com.chrizlove.vortexpay.payment.mapper.PaymentMapper;
import com.chrizlove.vortexpay.payment.repository.OrderRepository;
import com.chrizlove.vortexpay.payment.repository.PaymentRepository;
import com.chrizlove.vortexpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest orderRequest) {
        if(orderRequest.receipt()!=null && orderRepository.existsByMerchantIdAndReceipt(merchantId, orderRequest.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE","Order with receipt already exists"+orderRequest.receipt());
        }

        UUID customerId=null;
        if(orderRequest.customer()!=null){
            customerId=customerService.findOrCreate(merchantId,orderRequest.customer().email(),
                    orderRequest.customer().name(), orderRequest.customer().phone());
        }

        OrderRecord orderRecord = OrderRecord.builder().
                receipt(orderRequest.receipt()).
                amount(orderRequest.amount()).
                notes(orderRequest.notes()).
                merchantId(merchantId).
                customerId(customerId).
                orderStatus(OrderStatus.CREATED).
                expiresAt(orderRequest.expiresAt()!=null ? orderRequest.expiresAt() : LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes)).
                build();

        orderRecord = orderRepository.save(orderRecord);

        //TODO: Send kafka outbox event to db

        return orderMapper.toOrderResponse(orderRecord);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
       OrderRecord order=orderRepository.findByOrderIdAndMerchantId(orderId,merchantId).orElseThrow(()-> new ResourceNotFoundException("Order",orderId));
       return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord order=orderRepository.findByOrderIdAndMerchantId(orderId,merchantId).orElseThrow(()-> new ResourceNotFoundException("Order",orderId));
        if(order.getOrderStatus().equals(OrderStatus.CANCELED) || order.getOrderStatus().equals(OrderStatus.PAID)) {
            throw new BusinessRuleViolationException("CANNOT_CANCEL_ORDER", "Can't cancel order with status " + order.getOrderStatus());
        }
        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        //TODO: Send kafka outbox event to db

       return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByOrderIdAndMerchantId(orderId, merchantId).orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        List<Payment> paymentList = paymentRepository.findByOrderRecord_OrderId(order);

        return paymentMapper.toResponseList(paymentList);
    }

}
