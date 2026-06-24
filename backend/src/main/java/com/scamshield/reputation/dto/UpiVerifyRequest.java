package com.scamshield.reputation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiVerifyRequest {
    @NotBlank(message = "UPI ID cannot be blank")
    private String upiId;
}
