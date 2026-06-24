package com.scamshield.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudVerifyRequest {
    @NotBlank(message = "Entity type is required")
    private String type;
    
    @NotBlank(message = "Entity value is required")
    private String value;
}
