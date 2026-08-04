package com.chrizlove.vortexpay.payment.controller;

import com.chrizlove.vortexpay.merchant.security.MerchantContext;
import com.chrizlove.vortexpay.payment.dto.request.CreateOrderRequest;
import com.chrizlove.vortexpay.payment.dto.response.OrderResponse;
import com.chrizlove.vortexpay.payment.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final MerchantContext merchantContext;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create (@RequestBody @Valid CreateOrderRequest orderRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(merchantContext.getMerchantId(),orderRequest));
    }
}
