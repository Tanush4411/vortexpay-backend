package com.chrizlove.vortexpay.merchant_service.entity;

import com.chrizlove.vortexpay.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer",
        indexes = {@Index(name = "idx_customer_merchant_id",columnList = "merchant_id"),
        @Index(name = "idx_customer_email",columnList = "email")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String contactNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant  merchant;

    private LocalDateTime deletedAt;
}
