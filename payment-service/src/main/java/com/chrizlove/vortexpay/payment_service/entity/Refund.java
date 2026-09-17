package com.chrizlove.vortexpay.payment_service.entity;

import com.chrizlove.vortexpay.common_lib.entity.BaseEntity;
import com.chrizlove.vortexpay.common_lib.entity.Money;
import com.chrizlove.vortexpay.common_lib.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="payment_io", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private UUID merchantId;

    @Embedded
    @Enumerated(EnumType.STRING)
    private Money amount;

    @Embedded
    @Column(nullable = false)
    private RefundStatus refundStatus= RefundStatus.PENDING;

    @Column(length = 100)
    private String bankReference;

    @Column(length = 100)
    private String errorCode;

    @Column(length = 500)
    private String errorDescription;;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String,Object> notes;

    private LocalDateTime processedAt;



}
