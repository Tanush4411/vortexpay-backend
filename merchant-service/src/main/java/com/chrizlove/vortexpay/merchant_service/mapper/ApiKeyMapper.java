package com.chrizlove.vortexpay.merchant_service.mapper;


import com.chrizlove.vortexpay.merchant_service.dto.Response.ApiKeyCreateResponse;
import com.chrizlove.vortexpay.merchant_service.dto.Response.ApiKeyResponse;
import com.chrizlove.vortexpay.merchant_service.entity.ApiKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ApiKeyMapper {

    @Mapping(source = "keySecretHash", target = "keySecret")
    ApiKeyCreateResponse toApiKeyCreateResponse(ApiKey apiKey);

    List<ApiKeyResponse>  toApiKeyResponses(List<ApiKey> apiKeyList);
}
