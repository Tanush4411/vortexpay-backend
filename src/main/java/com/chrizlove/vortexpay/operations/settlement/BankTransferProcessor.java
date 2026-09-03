package com.chrizlove.vortexpay.operations.settlement;

import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.operations.settlement.dto.BankTransferResult;

import java.util.UUID;

public interface BankTransferProcessor {

    BankTransferResult initiate(UUID settlementId,UUID merchantId, Money amount, String bankAccount, String ifsc);
}
