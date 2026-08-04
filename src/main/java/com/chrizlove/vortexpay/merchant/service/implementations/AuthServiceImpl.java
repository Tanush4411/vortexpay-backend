package com.chrizlove.vortexpay.merchant.service.implementations;

import com.chrizlove.vortexpay.common.enums.MerchantStatus;
import com.chrizlove.vortexpay.common.enums.UserRole;
import com.chrizlove.vortexpay.common.exceptions.DuplicateResourceException;
import com.chrizlove.vortexpay.common.exceptions.ResourceNotFoundException;
import com.chrizlove.vortexpay.merchant.dto.Request.LoginRequest;
import com.chrizlove.vortexpay.merchant.dto.Request.MerchantSignupRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.LoginResponse;
import com.chrizlove.vortexpay.merchant.dto.Response.MerchantResponse;
import com.chrizlove.vortexpay.merchant.entity.AppUser;
import com.chrizlove.vortexpay.merchant.entity.Merchant;
import com.chrizlove.vortexpay.merchant.mapper.MerchantMapper;
import com.chrizlove.vortexpay.merchant.repository.AppUserRepository;
import com.chrizlove.vortexpay.merchant.repository.MerchantRepository;
import com.chrizlove.vortexpay.merchant.security.JwtUtil;
import com.chrizlove.vortexpay.merchant.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;

    private final MerchantRepository merchantRepository;

    private final MerchantMapper merchantMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;
    @Override
    @Transactional
    public MerchantResponse signup(MerchantSignupRequest request) {
        //Merchant existence check
        if(merchantRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL", "Merchant with this email already exists:"+ request.email());
        }

        //Creating a merchant and saving it to db
        Merchant merchant = merchantMapper.toMerchantEntity(request);
        merchant.setMerchantStatus(MerchantStatus.PENDING_KYC);
        merchant = merchantRepository.save(merchant);

        //Creating a appUser for the merchant
        AppUser appUser=AppUser.builder().
                email(request.email()).
                merchant(merchant).
                passwordHash(passwordEncoder.encode(request.password())).
                role(UserRole.OWNER).
                build();
        appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(merchant);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        //appUser authentication
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AppUser appUser= appUserRepository.findByEmail(request.email())
                .orElseThrow(()-> new ResourceNotFoundException("User", request.email()));

        //generating access token for the appUser
        String token=jwtUtil.generateAccessToken(request.email(),appUser.getMerchant().getId(),appUser.getRole().toString());
        return new LoginResponse(token);
    }
}
