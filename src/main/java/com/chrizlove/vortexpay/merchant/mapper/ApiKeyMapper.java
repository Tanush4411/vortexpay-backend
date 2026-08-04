package com.chrizlove.vortexpay.merchant.mapper;

import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyCreateResponse;
import com.chrizlove.vortexpay.merchant.dto.Response.ApiKeyResponse;
import com.chrizlove.vortexpay.merchant.entity.ApiKey;
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
