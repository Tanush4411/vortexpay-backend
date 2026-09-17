package com.chrizlove.vortexpay.vault_service.dto.response;


import com.chrizlove.vortexpay.common_lib.enums.CardBrand;

public record TokenizeResponse(
        String token,
        String lastFour,
        CardBrand cardBrand,
        Integer expiryMonth,
        Integer expiryYear

){
}
