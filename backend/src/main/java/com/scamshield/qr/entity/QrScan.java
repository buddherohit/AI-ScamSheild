package com.scamshield.qr.entity;

import com.scamshield.entity.BaseEntity;
import com.scamshield.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "qr_scans")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrScan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "extracted_upi", length = 255)
    private String extractedUpi;

    @Column(name = "extracted_merchant", length = 255)
    private String extractedMerchant;

    @Column(name = "risk_score")
    private int riskScore;

    @Column(name = "risk_level", length = 50)
    private String riskLevel;
}
