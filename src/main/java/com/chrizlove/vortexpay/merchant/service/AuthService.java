package com.chrizlove.vortexpay.merchant.service;


import com.chrizlove.vortexpay.merchant.dto.Request.LoginRequest;
import com.chrizlove.vortexpay.merchant.dto.Request.MerchantSignupRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.LoginResponse;
import com.chrizlove.vortexpay.merchant.dto.Response.MerchantResponse;

public interface AuthService {
     MerchantResponse signup(MerchantSignupRequest request);

     LoginResponse login(LoginRequest request);
}
