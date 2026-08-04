package com.chrizlove.vortexpay.merchant.mapper;

import com.chrizlove.vortexpay.merchant.dto.Request.MerchantSignupRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.MerchantResponse;
import com.chrizlove.vortexpay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MerchantMapper {

    Merchant toMerchantEntity(MerchantSignupRequest merchantSignupRequest);

    MerchantResponse toMerchantResponse(Merchant merchant);
}
