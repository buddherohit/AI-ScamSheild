package com.scamshield.fraud.entity;

import com.scamshield.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "threat_indicators")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatIndicator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, unique = true, length = 255)
    private String value;

    @Column(length = 100)
    private String source;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
