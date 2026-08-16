package com.chrizlove.vortexpay.merchant.service;

import com.chrizlove.vortexpay.merchant.dto.Request.UpdateWebhookConfigRequest;
import com.chrizlove.vortexpay.merchant.dto.Response.WebhookConfigResponse;

import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {

    WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request);

    List<WebhookConfigResponse> list(UUID merchantId);

    WebhookConfigResponse getById(UUID merchantId, UUID configId);

    WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateWebhookConfigRequest request);

    void delete(UUID merchantId, UUID configId);
}
