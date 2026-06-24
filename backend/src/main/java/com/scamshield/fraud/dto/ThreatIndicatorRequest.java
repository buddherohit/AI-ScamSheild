package com.scamshield.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatIndicatorRequest {

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Value is required")
    private String value;

    private String source;
}
