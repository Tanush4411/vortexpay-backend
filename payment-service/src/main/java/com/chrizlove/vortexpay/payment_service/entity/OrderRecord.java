package com.chrizlove.vortexpay.payment_service.entity;

import com.chrizlove.vortexpay.common_lib.entity.BaseEntity;
import com.chrizlove.vortexpay.common_lib.entity.Money;
import com.chrizlove.vortexpay.common_lib.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name="order_records",
        indexes = {@Index(name = "idx_order_id_merchant_id", columnList = "order_id, merchant_id"),
                @Index(name="idx_order_merchant_id", columnList = "merchant_id")}
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @Column(nullable = false, name = "merchant_id")
    private UUID merchantId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Embedded
    private Money amount;

    @Column(length = 100)
    private String receipt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus= OrderStatus.CREATED;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts=0;

    @JdbcTypeCode((SqlTypes.JSON))
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notes;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
