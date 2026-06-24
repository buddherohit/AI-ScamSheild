package com.scamshield.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudReportRequest {

    @NotBlank(message = "Entity type is required")
    @Pattern(regexp = "PHONE|UPI_ID|QR_CODE|EMAIL|WEBSITE|SMS|WHATSAPP|BANK_ACCOUNT", 
             message = "Invalid entity type. Must be one of PHONE, UPI_ID, QR_CODE, EMAIL, WEBSITE, SMS, WHATSAPP, BANK_ACCOUNT")
    private String entityType;

    @NotBlank(message = "Entity value is required")
    private String entityValue;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category code is required")
    private String categoryCode;

    @NotBlank(message = "Severity is required")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", 
             message = "Invalid severity level. Must be one of LOW, MEDIUM, HIGH, CRITICAL")
    private String severity;
}
