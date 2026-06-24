package com.scamshield.ai.service;

import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiExplanationEngine {

    @Getter
    @Builder
    public static class SmsExplanation {
        private final String riskReason;
        private final List<String> indicators;
        private final String userAction;
        private final String simpleExplanation;
        private final String technicalExplanation;
    }

    public SmsExplanation generateExplanation(
            String summary, 
            List<String> indicators, 
            String recommendation, 
            String category,
            int riskScore) {
            
        String simpleExplanation = summary;
        String technicalExplanation = String.format(
                "Natural Language Processing (NLP) classification engine flagged this message as category '%s' with a unified threat risk rating of %d/100. " +
                "Identified threat vector tags: %s. Mitigation strategy details: %s.",
                category, riskScore, String.join(", ", indicators), recommendation
        );

        return SmsExplanation.builder()
                .riskReason("SMS matches threat profile for " + category.replace("_", " "))
                .indicators(indicators)
                .userAction(recommendation)
                .simpleExplanation(simpleExplanation)
                .technicalExplanation(technicalExplanation)
                .build();
    }
}
