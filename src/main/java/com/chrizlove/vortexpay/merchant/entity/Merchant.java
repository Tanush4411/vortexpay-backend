package com.chrizlove.vortexpay.merchant.entity;

import com.chrizlove.vortexpay.common.entity.BaseEntity;
import com.chrizlove.vortexpay.common.enums.BusinessType;
import com.chrizlove.vortexpay.common.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "merchant",
        indexes = {@Index(name = "idx_merchant_status", columnList = "merchant_status")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    @Column(length = 200)
    private String businessName;

    @Column(length = 200)
    private String websiteURL;

    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MerchantStatus merchantStatus=MerchantStatus.PENDING_KYC;

    @Column(length = 20)
    private String gstId;

    @Column(length = 20)
    private String panId;

    @Column(length = 100)
    private String settlementBankAccount;

    @Column(length = 20)
    private String settlementBankName;

    @Column(length = 20)
    private String settlementBankIFSC;

    @Column(length = 100)
    private String settlementBankAccountHolderName;
}
