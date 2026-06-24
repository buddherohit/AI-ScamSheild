package com.scamshield.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.entity.User;
import com.scamshield.exception.BusinessException;
import com.scamshield.exception.ValidationException;
import com.scamshield.fraud.repository.ThreatIndicatorRepository;
import com.scamshield.repository.UserRepository;
import com.scamshield.ai.entity.*;
import com.scamshield.ai.provider.AIProvider;
import com.scamshield.ai.repository.*;
import com.scamshield.ai.dto.SmsAnalysisResponseDto;
import com.scamshield.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsAnalysisService {

    private final SmsAnalysisRepository smsAnalysisRepository;
    private final SmsAnalysisReasonRepository smsAnalysisReasonRepository;
    private final AiRequestRepository aiRequestRepository;
    private final AiResponseRepository aiResponseRepository;
    private final AnalysisHistoryRepository analysisHistoryRepository;

    private final UserRepository userRepository;
    private final ThreatIndicatorRepository threatIndicatorRepository;
    private final AIProvider aiProvider;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    private final PromptEngineeringService promptEngineeringService;
    private final FraudPatternEngine fraudPatternEngine;
    private final AiExplanationEngine aiExplanationEngine;

    private static final Pattern URL_PATTERN = Pattern.compile("(https?://[a-zA-Z0-9.\\-_/]+|www\\.[a-zA-Z0-9.\\-_/]+)");

    @Transactional
    public SmsAnalysisResponseDto analyzeSms(String smsText, String ipAddress, String userAgent) {
        if (smsText == null || smsText.trim().isEmpty()) {
            throw new ValidationException("SMS text cannot be empty", Map.of("smsText", "SMS text is required for analysis"));
        }

        String trimmedText = smsText.trim();
        long startTime = System.currentTimeMillis();

        // 1. Pre-analysis: Rule Engine
        FraudPatternEngine.PatternResult patternResult = fraudPatternEngine.evaluateText(trimmedText);

        // 2. Pre-analysis: Threat Intel check on URLs
        boolean hasBlacklistedLink = false;
        List<String> extractedUrls = new ArrayList<>();
        Matcher matcher = URL_PATTERN.matcher(trimmedText);
        while (matcher.find()) {
            String url = matcher.group(1);
            extractedUrls.add(url);
            // Check if threat indicator database holds this domain or link
            if (threatIndicatorRepository.existsByValueAndIsActiveTrue(url.toLowerCase())) {
                hasBlacklistedLink = true;
            }
        }

        // 3. AI Dispatch Setup
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("smsText", trimmedText);
        parameters.put("matchedIndicators", String.join(", ", patternResult.getMatchedIndicators()));
        parameters.put("ruleScore", patternResult.getRuleScore());

        String systemPrompt = promptEngineeringService.buildPrompt("SMS_SYSTEM_PROMPT", Map.of());
        String userPrompt = promptEngineeringService.buildPrompt("SMS_USER_PROMPT", parameters);

        // Log AI Request in database
        AiRequest aiRequest = AiRequest.builder()
                .provider(aiProvider.getProviderName())
                .model(aiProvider.getModelName())
                .prompt(userPrompt)
                .build();
        aiRequestRepository.save(aiRequest);

        // Call Provider
        String rawResponse = aiProvider.executePrompt(systemPrompt, userPrompt);
        long latency = System.currentTimeMillis() - startTime;

        // Log AI Response in database
        AiResponse aiResponse = AiResponse.builder()
                .request(aiRequest)
                .rawResponse(rawResponse)
                .latencyMs(latency)
                .build();
        aiResponseRepository.save(aiResponse);

        // 4. Parse AI JSON Response
        int aiScore = 0;
        String aiLevel = "LOW";
        String aiCategory = "UNKNOWN";
        String summary = "Unable to classify message";
        String recommendation = "Review carefully before action";
        List<String> indicators = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            aiScore = rootNode.path("riskScore").asInt();
            aiLevel = rootNode.path("riskLevel").asText("LOW");
            aiCategory = rootNode.path("category").asText("UNKNOWN");
            summary = rootNode.path("summary").asText(summary);
            recommendation = rootNode.path("recommendation").asText(recommendation);

            JsonNode indNode = rootNode.path("indicators");
            if (indNode.isArray()) {
                for (JsonNode node : indNode) {
                    indicators.add(node.asText());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse JSON response from AI: {}", rawResponse, e);
            // Fallback to rules findings if JSON parsing completely fails
            aiScore = patternResult.getRuleScore();
            aiLevel = getRiskLevelForScore(aiScore);
            aiCategory = patternResult.getDominantCategory();
            summary = "Analysis conducted via local rules engine due to LLM parsing exception.";
            indicators.addAll(patternResult.getMatchedIndicators());
        }

        // 5. Integrate Rule-Engine, AI results, and URL override triggers
        int finalRiskScore = Math.max(aiScore, patternResult.getRuleScore());
        if (hasBlacklistedLink) {
            finalRiskScore = 100;
            indicators.add("Blacklisted Threat Link");
            summary = "CRITICAL: This SMS contains a verified threat link blacklisted by System Intelligence.";
            recommendation = "Do NOT click the link. Immediately delete the message.";
        }

        finalRiskScore = Math.max(0, Math.min(100, finalRiskScore));
        String finalRiskLevel = getRiskLevelForScore(finalRiskScore);

        // Fetch User context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);

        // 6. Save Analysis Result
        SmsAnalysis analysis = SmsAnalysis.builder()
                .user(currentUser)
                .smsText(trimmedText)
                .riskScore(finalRiskScore)
                .riskLevel(finalRiskLevel)
                .category(aiCategory)
                .summary(summary)
                .recommendation(recommendation)
                .build();
        smsAnalysisRepository.save(analysis);

        // Save Reasons
        List<SmsAnalysisReason> reasons = new ArrayList<>();
        // Merge rules-based and AI indicators
        Set<String> allIndicators = new LinkedHashSet<>(patternResult.getMatchedIndicators());
        allIndicators.addAll(indicators);

        for (String ind : allIndicators) {
            SmsAnalysisReason reason = SmsAnalysisReason.builder()
                    .analysis(analysis)
                    .reason(ind)
                    .build();
            smsAnalysisReasonRepository.save(reason);
            reasons.add(reason);
        }

        // Save aggregated history
        String previewText = trimmedText.length() > 60 ? trimmedText.substring(0, 57) + "..." : trimmedText;
        AnalysisHistory history = AnalysisHistory.builder()
                .user(currentUser)
                .smsTextPreview(previewText)
                .riskScore(finalRiskScore)
                .riskLevel(finalRiskLevel)
                .category(aiCategory)
                .build();
        analysisHistoryRepository.save(history);

        // Audit Logging
        if (currentUser != null) {
            auditService.logAction(currentUser.getId(), "ANALYZE_SMS", ipAddress, userAgent);
        }

        // 7. Generate layperson-friendly vs technical explainers
        AiExplanationEngine.SmsExplanation explanation = aiExplanationEngine.generateExplanation(
                summary, new ArrayList<>(allIndicators), recommendation, aiCategory, finalRiskScore
        );

        return SmsAnalysisResponseDto.builder()
                .id(analysis.getId())
                .smsText(trimmedText)
                .riskScore(finalRiskScore)
                .riskLevel(finalRiskLevel)
                .category(aiCategory)
                .summary(summary)
                .recommendation(recommendation)
                .indicators(new ArrayList<>(allIndicators))
                .simpleExplanation(explanation.getSimpleExplanation())
                .technicalExplanation(explanation.getTechnicalExplanation())
                .createdAt(analysis.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<SmsAnalysisResponseDto> getHistory(String search, Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) {
            return Page.empty();
        }

        Page<SmsAnalysis> page;
        if (search != null && !search.trim().isEmpty()) {
            page = smsAnalysisRepository.searchByUserId(currentUser.getId(), search.trim(), pageable);
        } else {
            page = smsAnalysisRepository.findByUserId(currentUser.getId(), pageable);
        }

        return page.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SmsAnalysisResponseDto getAnalysisDetails(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        
        SmsAnalysis analysis = smsAnalysisRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SMS analysis record not found."));

        if (currentUser != null && !currentUser.getId().equals(analysis.getUser().getId())) {
            throw new BusinessException("You are not authorized to view this analysis.");
        }

        return mapToResponse(analysis);
    }

    @Transactional
    public void deleteAnalysis(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        
        SmsAnalysis analysis = smsAnalysisRepository.findById(id)
                .orElseThrow(() -> new BusinessException("SMS analysis record not found."));

        if (currentUser != null && !currentUser.getId().equals(analysis.getUser().getId())) {
            throw new BusinessException("You are not authorized to delete this analysis.");
        }

        // Delete from SmsAnalysis (cascades to reasons due to table constraint)
        smsAnalysisRepository.delete(analysis);

        // Also clean up AnalysisHistory if there's matching score/category
        List<AnalysisHistory> histories = analysisHistoryRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        String previewText = analysis.getSmsText().length() > 60 ? analysis.getSmsText().substring(0, 57) + "..." : analysis.getSmsText();
        for (AnalysisHistory hist : histories) {
            if (hist.getSmsTextPreview().equals(previewText) && hist.getRiskScore() == analysis.getRiskScore()) {
                analysisHistoryRepository.delete(hist);
                break;
            }
        }
    }

    private SmsAnalysisResponseDto mapToResponse(SmsAnalysis s) {
        List<String> indicators = smsAnalysisReasonRepository.findByAnalysisId(s.getId()).stream()
                .map(SmsAnalysisReason::getReason)
                .collect(Collectors.toList());

        AiExplanationEngine.SmsExplanation explanation = aiExplanationEngine.generateExplanation(
                s.getSummary(), indicators, s.getRecommendation(), s.getCategory(), s.getRiskScore()
        );

        return SmsAnalysisResponseDto.builder()
                .id(s.getId())
                .smsText(s.getSmsText())
                .riskScore(s.getRiskScore())
                .riskLevel(s.getRiskLevel())
                .category(s.getCategory())
                .summary(s.getSummary())
                .recommendation(s.getRecommendation())
                .indicators(indicators)
                .simpleExplanation(explanation.getSimpleExplanation())
                .technicalExplanation(explanation.getTechnicalExplanation())
                .createdAt(s.getCreatedAt())
                .build();
    }

    private String getRiskLevelForScore(int score) {
        if (score <= 30) return "LOW";
        if (score <= 60) return "MEDIUM";
        if (score <= 80) return "HIGH";
        return "CRITICAL";
    }
}
