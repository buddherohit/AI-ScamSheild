package com.scamshield.reputation.entity;

import com.scamshield.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchant_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upi_profile_id", nullable = false)
    private UpiProfile upiProfile;

    @Column(name = "merchant_name", nullable = false)
    private String merchantName;

    @Column(name = "mcc", length = 10)
    private String mcc;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;
}
