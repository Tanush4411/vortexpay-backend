package com.chrizlove.vortexpay.operations_service.settlement;

import com.chrizlove.vortexpay.common_lib.enums.SettlementStatus;
import com.chrizlove.vortexpay.operations_service.entity.Settlement;
import com.chrizlove.vortexpay.operations_service.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BankSettlementCallbackSimulator {

    private final SettlementRepository settlementRepository;
    private final SettlementTransactionExecutor  settlementTransactionExecutor;

    @Scheduled(fixedDelayString = "5000")
    public void processCallbacks(){
        List<Settlement> settlements = settlementRepository.findBySettlementStatus(SettlementStatus.TRANSFER_PENDING);

        if(settlements.isEmpty()) return;

        for(Settlement settlement : settlements){
            simulateCallback(settlement);
        }
    }

    private void simulateCallback(Settlement settlement){
        log.info("Initiating settlement callback for settlementId: {}", settlement.getId());

        settlementTransactionExecutor.resolveTransfer(settlement.getId(), null,null);
    }
}
