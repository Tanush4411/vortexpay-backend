package com.chrizlove.vortexpay.operations.entity;

import com.chrizlove.vortexpay.common.entity.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "dlq_events")
public class DlqEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @OneToOne(fetch = FetchType.LAZY)
    private WebhookEvent webhookEvent;

    @Column(length = 1000)
    private String finalError;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    private LocalDateTime movedAt;

    private LocalDateTime replayedAt;
}
