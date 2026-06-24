package com.scamshield.reputation.entity;

import com.scamshield.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "upi_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "upi_id", nullable = false, unique = true)
    private String upiId;

    @Column(name = "normalized_upi", nullable = false)
    private String normalizedUpi;

    @Column(name = "risk_score", nullable = false)
    @Builder.Default
    private int riskScore = 0;

    @Column(name = "risk_level", nullable = false, length = 50)
    @Builder.Default
    private String riskLevel = "SAFE";
}
