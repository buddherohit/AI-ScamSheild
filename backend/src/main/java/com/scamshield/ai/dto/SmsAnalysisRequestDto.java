package com.scamshield.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsAnalysisRequestDto {
    @NotBlank(message = "SMS text cannot be blank")
    private String smsText;
}
