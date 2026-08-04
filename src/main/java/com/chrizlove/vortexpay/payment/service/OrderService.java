package com.chrizlove.vortexpay.payment.service;

import com.chrizlove.vortexpay.payment.dto.request.CreateOrderRequest;
import com.chrizlove.vortexpay.payment.dto.response.OrderResponse;
import com.chrizlove.vortexpay.payment.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
     OrderResponse create(UUID merchantId, CreateOrderRequest orderRequest);

     OrderResponse getById(UUID merchantId, UUID orderId);

     OrderResponse cancel(UUID merchantId, UUID orderId);

     List<PaymentResponse> listPayments(UUID merchantId, UUID orderId);
}
