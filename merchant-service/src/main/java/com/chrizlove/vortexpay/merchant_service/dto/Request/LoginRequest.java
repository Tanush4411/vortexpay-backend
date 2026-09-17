package com.chrizlove.vortexpay.merchant_service.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(

        @NotNull @Email
        String email,

        @NotBlank
        String password
) {
}
