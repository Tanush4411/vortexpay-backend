package com.chrizlove.vortexpay.merchant_service.controller;

import com.chrizlove.vortexpay.merchant_service.dto.Request.LoginRequest;
import com.chrizlove.vortexpay.merchant_service.dto.Request.MerchantSignupRequest;
import com.chrizlove.vortexpay.merchant_service.dto.Response.LoginResponse;
import com.chrizlove.vortexpay.merchant_service.dto.Response.MerchantResponse;
import com.chrizlove.vortexpay.merchant_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

//    @PostMapping("/signup")
//    public ResponseEntity<MerchantResponse> signUp(@RequestBody @Valid MerchantSignupRequest request){
//     return  ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
//    }

    @PostMapping("/signup")
    public ResponseEntity<MerchantResponse> signUp(@RequestBody @Valid MerchantSignupRequest request) {
        log.info("Signup request: {}", request);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
        } catch (Exception e) {
            e.printStackTrace(); // Forces the exact stack trace to print in your console
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request){
        return  ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}
