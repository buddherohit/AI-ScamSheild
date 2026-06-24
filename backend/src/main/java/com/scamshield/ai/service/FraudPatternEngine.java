package com.scamshield.ai.service;

import com.scamshield.ai.entity.FraudPattern;
import com.scamshield.ai.repository.FraudPatternRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudPatternEngine {

    private final FraudPatternRepository fraudPatternRepository;

    @Getter
    public static class PatternResult {
        private final List<String> matchedIndicators = new ArrayList<>();
        private final int ruleScore;
        private final String dominantCategory;

        public PatternResult(List<String> matchedIndicators, int ruleScore, String dominantCategory) {
            this.matchedIndicators.addAll(matchedIndicators);
            this.ruleScore = Math.min(100, Math.max(0, ruleScore));
            this.dominantCategory = dominantCategory != null ? dominantCategory : "UNKNOWN";
        }
    }

    public PatternResult evaluateText(String smsText) {
        if (smsText == null || smsText.trim().isEmpty()) {
            return new PatternResult(List.of(), 0, "UNKNOWN");
        }

        List<FraudPattern> patterns = fraudPatternRepository.findAll();
        List<String> matched = new ArrayList<>();
        int totalScore = 0;
        String dominantCategory = "UNKNOWN";
        int maxWeight = -1;

        for (FraudPattern pattern : patterns) {
            try {
                Pattern regex = Pattern.compile(pattern.getPatternRegex());
                if (regex.matcher(smsText).find()) {
                    matched.add(pattern.getPatternKey());
                    totalScore += pattern.getWeight();
                    if (pattern.getWeight() > maxWeight) {
                        maxWeight = pattern.getWeight();
                        dominantCategory = pattern.getCategory();
                    }
                }
            } catch (Exception e) {
                log.error("Invalid regex pattern: {} -> {}", pattern.getPatternKey(), pattern.getPatternRegex(), e);
            }
        }

        return new PatternResult(matched, totalScore, dominantCategory);
    }
}
