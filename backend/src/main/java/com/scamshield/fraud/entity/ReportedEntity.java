package com.scamshield.fraud.entity;

import com.scamshield.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reported_entities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"type", "value"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportedEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 255)
    private String value;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
