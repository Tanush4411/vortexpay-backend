package com.chrizlove.vortexpay.merchant_service.mapper;

import com.chrizlove.vortexpay.merchant_service.dto.Request.MerchantSignupRequest;
import com.chrizlove.vortexpay.merchant_service.dto.Response.MerchantResponse;
import com.chrizlove.vortexpay.merchant_service.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toMerchantEntity(MerchantSignupRequest merchantSignupRequest);

    MerchantResponse toMerchantResponse(Merchant merchant);
}
