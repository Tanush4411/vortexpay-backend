package com.chrizlove.vortexpay.vault_service.entity;

import com.chrizlove.vortexpay.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="card_tokens")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 50,unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="vault_card_id", nullable = false)
    private VaultCard vaultCard;

    private UUID customerId;

    @Column(nullable = false)
    private UUID merchantId;

    private LocalDateTime revokedAt;
}
