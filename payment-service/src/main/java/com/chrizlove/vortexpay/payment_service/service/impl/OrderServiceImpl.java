package com.chrizlove.vortexpay.payment_service.service.impl;

import com.chrizlove.vortexpay.common_lib.dto.FindOrCreateCustomerRequest;
import com.chrizlove.vortexpay.common_lib.enums.EventAggregateType;
import com.chrizlove.vortexpay.common_lib.enums.OrderStatus;
import com.chrizlove.vortexpay.common_lib.exceptions.BusinessRuleViolationException;
import com.chrizlove.vortexpay.common_lib.exceptions.DuplicateResourceException;
import com.chrizlove.vortexpay.common_lib.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.payment_service.client.CustomerServiceClient;
import com.chrizlove.vortexpay.payment_service.dto.request.CreateOrderRequest;
import com.chrizlove.vortexpay.payment_service.dto.response.OrderResponse;
import com.chrizlove.vortexpay.payment_service.dto.response.PaymentResponse;
import com.chrizlove.vortexpay.payment_service.entity.OrderRecord;
import com.chrizlove.vortexpay.payment_service.entity.Payment;
import com.chrizlove.vortexpay.payment_service.mapper.OrderMapper;
import com.chrizlove.vortexpay.payment_service.mapper.PaymentMapper;
import com.chrizlove.vortexpay.payment_service.outbox.OutboxEventPublisher;
import com.chrizlove.vortexpay.payment_service.repository.OrderRepository;
import com.chrizlove.vortexpay.payment_service.repository.PaymentRepository;
import com.chrizlove.vortexpay.payment_service.service.OrderService;
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
    private final CustomerServiceClient customerServiceClient;
    private final OutboxEventPublisher eventPublisher;

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
            customerId=customerServiceClient.findOrCreate(
                    new FindOrCreateCustomerRequest(merchantId,orderRequest.customer().email(),
                    orderRequest.customer().name(), orderRequest.customer().phone()));
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

        //pushing the kafka event to our db
        eventPublisher.publish(EventAggregateType.ORDER,orderRecord.getOrderId(),"ORDER_CREATED",
                Map.of("orderId",orderRecord.getOrderId(),
                        "merchantId",orderRecord.getMerchantId().toString(),
                        "orderStatus", orderRecord.getOrderStatus().name(),
                        "amountUnits",orderRecord.getAmount().getAmountUnits(),
                        "amountCurrency",orderRecord.getAmount().getCurrency())
        );

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

        //pushing the kafka event to our db
        eventPublisher.publish(EventAggregateType.ORDER,order.getOrderId(),"ORDER_CANCELLED",
                Map.of("orderId",order.getOrderId(),
                        "merchantId",order.getMerchantId().toString(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits",order.getAmount().getAmountUnits(),
                        "amountCurrency",order.getAmount().getCurrency())
        );

       return orderMapper.toOrderResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {
        OrderRecord order = orderRepository.findByOrderIdAndMerchantId(orderId, merchantId).orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        List<Payment> paymentList = paymentRepository.findByOrderRecord_OrderId(order);

        return paymentMapper.toResponseList(paymentList);
    }

}
