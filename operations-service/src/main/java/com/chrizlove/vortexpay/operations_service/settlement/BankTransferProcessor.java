package com.chrizlove.vortexpay.operations_service.settlement;


import com.chrizlove.vortexpay.common_lib.entity.Money;
import com.chrizlove.vortexpay.operations_service.settlement.dto.BankTransferResult;

import java.util.UUID;

public interface BankTransferProcessor {

    BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc);
}
