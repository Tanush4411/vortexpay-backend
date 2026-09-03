package com.chrizlove.vortexpay.operations.repository;

import com.chrizlove.vortexpay.common.enums.SettlementStatus;
import com.chrizlove.vortexpay.operations.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findBySettlementStatus(SettlementStatus settlementStatus);
}
