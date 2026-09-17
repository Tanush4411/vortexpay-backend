package com.chrizlove.vortexpay.payment_service.entity;

import com.chrizlove.vortexpay.common_lib.entity.BaseEntity;
import com.chrizlove.vortexpay.common_lib.entity.Money;
import com.chrizlove.vortexpay.common_lib.enums.PaymentMethod;
import com.chrizlove.vortexpay.common_lib.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name="payments",
        indexes = {@Index(name = "idx_payment_order_id",columnList = "order_id"),
        @Index(name="idx_payment_merchant_id", columnList = "merchant_id")}
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderRecord orderRecord;

    @Column(nullable = false)
    private UUID merchantId;

    @Embedded
    private Money amount;

    @Column(nullable = false, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @JdbcTypeCode((SqlTypes.JSON))
    @Column(columnDefinition = "jsonb")
    private Map<String,Object> methodDetails;

    @Column( length = 100)
    private String bankReference;

    @Column( length = 100)
    private String processorReference;

    @Column( length = 100)
    private String errorCode;

    @Column( length = 250)
    private String errorDescription;;


    private LocalDateTime authorizedAt;

    private LocalDateTime capturedAt;

    private LocalDateTime failedAt;

    private LocalDateTime refundedAt;

    private LocalDateTime settledAt;

}
