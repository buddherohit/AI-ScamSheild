package com.scamshield.reputation.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpiVerifyResponse {
    private String upiId;
    private int score;
    private String status;
    private List<String> reasons;
}
