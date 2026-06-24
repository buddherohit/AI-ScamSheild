package com.scamshield.fraud.entity;

import com.scamshield.entity.BaseEntity;
import com.scamshield.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fraud_reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entity_id", nullable = false)
    private ReportedEntity entity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private FraudCategory category;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String severity;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";
}
