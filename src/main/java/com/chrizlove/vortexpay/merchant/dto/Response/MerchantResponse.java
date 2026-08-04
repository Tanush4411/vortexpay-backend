package com.chrizlove.vortexpay.merchant.dto.Response;


import com.chrizlove.vortexpay.common.enums.BusinessType;
import com.chrizlove.vortexpay.common.enums.MerchantStatus;

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
