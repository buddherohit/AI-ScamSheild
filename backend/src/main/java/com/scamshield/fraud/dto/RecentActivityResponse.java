package com.scamshield.fraud.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {
    private String id;
    private String timestamp;
    private String type;
    private String severity;
    private String description;
    private String status;
}
