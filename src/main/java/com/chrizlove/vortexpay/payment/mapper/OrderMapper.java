package com.chrizlove.vortexpay.payment.mapper;

import com.chrizlove.vortexpay.payment.dto.response.OrderResponse;
import com.chrizlove.vortexpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(source = "orderId", target = "id")
    OrderResponse toOrderResponse(OrderRecord order);
}
