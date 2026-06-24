package com.scamshield.ai.entity;

import com.scamshield.entity.BaseEntity;
import com.scamshield.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sms_analysis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "sms_text", nullable = false, columnDefinition = "TEXT")
    private String smsText;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "risk_level", nullable = false, length = 50)
    private String riskLevel;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "recommendation", nullable = false, columnDefinition = "TEXT")
    private String recommendation;
}
