package com.chrizlove.vortexpay.vault.dto.response;


import com.chrizlove.vortexpay.common.enums.CardBrand;

public record TokenizeResponse(
        String token,
        String lastFour,
        CardBrand cardBrand,
        Integer expiryMonth,
        Integer expiryYear

){
}
