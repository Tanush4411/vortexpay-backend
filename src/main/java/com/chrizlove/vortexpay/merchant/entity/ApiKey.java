package com.chrizlove.vortexpay.merchant.entity;

import com.chrizlove.vortexpay.common.entity.BaseEntity;
import com.chrizlove.vortexpay.common.enums.ApiEnvironment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_key",
        indexes = {@Index(name = "idx_api_key_merchant_env", columnList = "merchant_id, api_environment, enabled")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="merchant_id",nullable = false)
    private Merchant merchant;

    @Column(nullable = false,length = 200)
    private String keySecretHash;

    @Column(length = 200)
    private String previousKeySecretHash;

    @Column(nullable = false,length = 100,unique = true)
    private String keyId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApiEnvironment apiEnvironment;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled=true;

    private LocalDateTime lastUsedAt;
    private LocalDateTime rotatedAt;
    private  LocalDateTime gracePeriodExpiresAt;

    public boolean isInGracePeriod() {
        return gracePeriodExpiresAt!=null && LocalDateTime.now().isBefore(gracePeriodExpiresAt);
    }
}
