package com.chrizlove.vortexpay.payment_service.controller;

import com.chrizlove.vortexpay.common_lib.context.MerchantContext;
import com.chrizlove.vortexpay.payment_service.dto.request.CreateOrderRequest;
import com.chrizlove.vortexpay.payment_service.dto.response.OrderResponse;
import com.chrizlove.vortexpay.payment_service.service.OrderService;
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
