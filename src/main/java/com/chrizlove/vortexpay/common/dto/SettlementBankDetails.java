package com.chrizlove.vortexpay.common.dto;

public record SettlementBankDetails(
        String accountNumber,

       String ifsc,

       String accountHolderName
) {
}
