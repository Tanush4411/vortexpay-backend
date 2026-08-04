package com.chrizlove.vortexpay.merchant.cache;


import com.chrizlove.vortexpay.common.enums.ApiEnvironment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyCacheEntry(
        String keyId,
        String keySecretHash,
        String previousKeySecretHash,
        LocalDateTime gracePeriodExpiresAt,
        UUID merchantId,
        ApiEnvironment apiEnvironment,
        boolean enabled

) {

    public boolean isInGracePeriod() {
        return gracePeriodExpiresAt!=null && LocalDateTime.now().isBefore(gracePeriodExpiresAt);
    }
}
