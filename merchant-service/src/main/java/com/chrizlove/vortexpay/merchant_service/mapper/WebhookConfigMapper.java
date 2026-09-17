package com.chrizlove.vortexpay.merchant_service.mapper;

import com.chrizlove.vortexpay.merchant_service.dto.Response.WebhookConfigResponse;
import com.chrizlove.vortexpay.merchant_service.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WebhookConfigMapper {

    @Mapping(target = "webhookSecret", source = "rawSecret")
    WebhookConfigResponse toResponse(MerchantWebhookConfig merchantWebhookConfig, String rawSecret);

}
