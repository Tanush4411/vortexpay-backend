package com.chrizlove.vortexpay.operations_service.settlement.impl;

import com.chrizlove.vortexpay.common_lib.entity.Money;
import com.chrizlove.vortexpay.common_lib.utils.RandomizerUtil;
import com.chrizlove.vortexpay.operations_service.settlement.BankTransferProcessor;
import com.chrizlove.vortexpay.operations_service.settlement.dto.BankTransferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class BankTransferProcessorImpl implements BankTransferProcessor {

    @Override
    public BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc) {

        //Call the bank server/api

        String registrationRef = "TXN_"+ RandomizerUtil.randomBase64(12);

        log.debug("Bank Transfer call completed for settlementId: {}, registrationRef: {}",
                settlementId, registrationRef);
        return new BankTransferResult(registrationRef);
    }
}
