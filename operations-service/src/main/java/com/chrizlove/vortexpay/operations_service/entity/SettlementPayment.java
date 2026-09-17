package com.chrizlove.vortexpay.operations_service.entity;

import com.chrizlove.vortexpay.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="settlement_payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementPayment extends BaseEntity {
    @EmbeddedId
    private SettlementPaymentId id;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;
}
