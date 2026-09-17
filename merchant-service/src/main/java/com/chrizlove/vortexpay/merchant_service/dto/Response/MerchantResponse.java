package com.chrizlove.vortexpay.merchant_service.dto.Response;


import com.chrizlove.vortexpay.common_lib.enums.BusinessType;
import com.chrizlove.vortexpay.common_lib.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,

        String name,

        String email,

        String businessName,

        BusinessType businessType,

        MerchantStatus merchantStatus

        ) {
}
