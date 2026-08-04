package com.chrizlove.vortexpay.merchant.controller;

import com.chrizlove.vortexpay.merchant.dto.Request.LoginRequest;
import com.chrizlove.vortexpay.merchant.dto.Request.MerchantSignupRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.LoginResponse;
import com.chrizlove.vortexpay.merchant.dto.Response.MerchantResponse;
import com.chrizlove.vortexpay.merchant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MerchantResponse> signUp(@RequestBody @Valid MerchantSignupRequest request){
     return  ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        return  ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}
