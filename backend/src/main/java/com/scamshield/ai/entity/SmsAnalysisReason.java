package com.scamshield.ai.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sms_analysis_reasons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsAnalysisReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private SmsAnalysis analysis;

    @Column(name = "reason", nullable = false)
    private String reason;
}
