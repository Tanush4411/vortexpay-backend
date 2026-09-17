package com.chrizlove.vortexpay.payment_service.mapper;


import com.chrizlove.vortexpay.payment_service.dto.response.OrderResponse;
import com.chrizlove.vortexpay.payment_service.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(source = "orderId", target = "id")
    OrderResponse toOrderResponse(OrderRecord order);
}
