package com.chrizlove.vortexpay.operations.entity;

import com.chrizlove.vortexpay.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name="settlement_payments")
public class SettlementPayment extends BaseEntity {
    @EmbeddedId
    private SettlementPaymentId id;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;
}
