package com.chrizlove.vortexpay.merchant_service.repository;

import com.chrizlove.vortexpay.merchant_service.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
}
