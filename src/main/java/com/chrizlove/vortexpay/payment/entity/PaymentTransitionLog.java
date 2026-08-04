package com.chrizlove.vortexpay.payment.entity;

import com.chrizlove.vortexpay.common.entity.BaseEntity;
import com.chrizlove.vortexpay.common.enums.PaymentActor;
import com.chrizlove.vortexpay.common.enums.PaymentEvent;
import com.chrizlove.vortexpay.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="payment_transition_logs",
        indexes = {@Index(name = "idx_payment_transition_log_payment_id", columnList = "payment_id")}
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransitionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="from_status")
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="to_status")
    private PaymentStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="actor", length=100)
    private PaymentActor actor;

    @Column(name="occurred_at", nullable=false)
    private LocalDateTime occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name="event", nullable=false)
    private PaymentEvent paymentEvent;
}
