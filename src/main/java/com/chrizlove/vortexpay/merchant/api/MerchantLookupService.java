package com.chrizlove.vortexpay.merchant.api;

import com.chrizlove.vortexpay.common.dto.SettlementBankDetails;
import com.chrizlove.vortexpay.common.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {

    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType);

    List<UUID> listActiveMerchantIds();

    SettlementBankDetails getSettlementBankDetails(UUID merchantId);
}
