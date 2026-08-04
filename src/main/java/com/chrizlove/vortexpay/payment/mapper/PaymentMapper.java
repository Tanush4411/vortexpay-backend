package com.chrizlove.vortexpay.payment.mapper;

import com.chrizlove.vortexpay.payment.dto.response.PaymentResponse;
import com.chrizlove.vortexpay.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "orderRecord.orderId")
    PaymentResponse toResponse(Payment payment);

    //@Mapping(target = "orderId", source = "orderRecord.orderId")
    List<PaymentResponse> toResponseList(List<Payment> paymentList);
}
