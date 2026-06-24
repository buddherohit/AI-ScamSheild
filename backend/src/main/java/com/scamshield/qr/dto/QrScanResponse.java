package com.scamshield.qr.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrScanResponse {
    private String merchant;
    private String upi;
    private int riskScore;
    private String riskLevel;
    private String recommendation;
}
