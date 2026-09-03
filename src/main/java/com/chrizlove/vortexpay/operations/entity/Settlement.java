package com.chrizlove.vortexpay.operations.entity;

import com.chrizlove.vortexpay.common.entity.BaseEntity;
import com.chrizlove.vortexpay.common.entity.Money;
import com.chrizlove.vortexpay.common.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="settlements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="amountUnits", column= @Column(name="gross_amount units", nullable=false)),
            @AttributeOverride(name="currency", column = @Column(name = "gross_amount_currency", nullable=false))
    })
    private Money grossAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="amountUnits", column= @Column(name="refund_amount units", nullable=false)),
            @AttributeOverride(name="currency", column = @Column(name = "refund_amount_currency", nullable=false))
    })
    private Money refundAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="amountUnits", column= @Column(name="gst_amount units", nullable=false)),
            @AttributeOverride(name="currency", column = @Column(name = "gst_amount_currency", nullable=false))
    })
    private Money gstAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="amountUnits", column= @Column(name="fee_amount units", nullable=false)),
            @AttributeOverride(name="currency", column = @Column(name = "fee_amount_currency", nullable=false))
    })
    private Money feeAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="amountUnits", column= @Column(name="net_amount units", nullable=false)),
            @AttributeOverride(name="currency", column = @Column(name = "net_amount_currency", nullable=false))
    })
    private Money netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus settlementStatus;

    @Column(nullable = false,length = 100)
    private String bankReference;

    private LocalDateTime processedAt;

    private String failureReason;
}
