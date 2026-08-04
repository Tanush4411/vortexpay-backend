package com.chrizlove.vortexpay.merchant.dto.Request;

import com.chrizlove.vortexpay.common.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest(
        @NotNull(message = "Name should be provided")
        @Size(max=50, message = "Name should be under 50 characters")
        String name,

        @Email
        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        @Size(min = 8, message = "Password should be minimum 8 characters long")
        String password,

        @Size(max = 100, message = "Business name should not exceed 100 characters")
        String businessName,

        BusinessType businessType
) {
}
