package com.scamshield.qr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrVerifyRequest {
    @NotBlank(message = "Raw QR text cannot be blank")
    private String rawText;
}
