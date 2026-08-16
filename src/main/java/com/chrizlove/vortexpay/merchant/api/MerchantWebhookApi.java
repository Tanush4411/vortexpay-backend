package com.chrizlove.vortexpay.merchant.api;

import com.chrizlove.vortexpay.common.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantWebhookApi {

    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType);
}
