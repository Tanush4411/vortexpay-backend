package com.chrizlove.vortexpay.merchant.entity;

import com.chrizlove.vortexpay.common.entity.BaseEntity;
import com.chrizlove.vortexpay.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "app_user",
        indexes = {@Index(name = "idx_app_user_merchant_id",columnList = "merchant_id")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false,length = 100)
    private String passwordHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
}
