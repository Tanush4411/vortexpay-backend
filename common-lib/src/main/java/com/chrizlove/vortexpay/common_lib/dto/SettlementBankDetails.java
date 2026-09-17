package com.chrizlove.vortexpay.common_lib.dto;

public record SettlementBankDetails(
        String accountNumber,

       String ifsc,

       String accountHolderName
) {
}
