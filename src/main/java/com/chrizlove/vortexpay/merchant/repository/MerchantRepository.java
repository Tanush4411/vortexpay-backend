package com.chrizlove.vortexpay.merchant.repository;

import com.chrizlove.vortexpay.common.enums.MerchantStatus;
import com.chrizlove.vortexpay.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface MerchantRepository extends JpaRepository<Merchant, UUID>{
    boolean existsByEmail(String email);

    List<Merchant> findByMerchantStatus(MerchantStatus status);
}
