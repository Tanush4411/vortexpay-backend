package com.chrizlove.vortexpay.merchant_service.service;


import com.chrizlove.vortexpay.merchant_service.dto.Request.LoginRequest;
import com.chrizlove.vortexpay.merchant_service.dto.Request.MerchantSignupRequest;
import com.chrizlove.vortexpay.merchant_service.dto.Response.LoginResponse;
import com.chrizlove.vortexpay.merchant_service.dto.Response.MerchantResponse;

public interface AuthService {
     MerchantResponse signup(MerchantSignupRequest request);

     LoginResponse login(LoginRequest request);
}
