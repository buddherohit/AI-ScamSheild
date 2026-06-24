package com.scamshield.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAIProvider implements AIProvider {

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIProvider(
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${OPENAI_MODEL:gpt-4o-mini}") String model,
            ObjectMapper objectMapper) {
        // Fallback to reading directly from System Env if Spring Value is default/empty
        String envKey = System.getenv("OPENAI_API_KEY");
        this.apiKey = (envKey != null && !envKey.trim().isEmpty()) ? envKey : apiKey;
        
        String envModel = System.getenv("OPENAI_MODEL");
        this.model = (envModel != null && !envModel.trim().isEmpty()) ? envModel : model;
        
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String executePrompt(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("OpenAI API Key is missing. Returning a mocked safe response for testing.");
            return mockResponse(userPrompt);
        }

        int maxRetries = 3;
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            attempt++;
            try {
                // Construct request payload
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                
                List<Map<String, String>> messages = new ArrayList<>();
                messages.add(Map.of("role", "system", "content", systemPrompt));
                messages.add(Map.of("role", "user", "content", userPrompt));
                requestBody.put("messages", messages);
                
                // Enforce JSON Object response format
                requestBody.put("response_format", Map.of("type", "json_object"));

                String jsonString = objectMapper.writeValueAsString(requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .timeout(Duration.ofSeconds(15))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    return root.path("choices").get(0).path("message").path("content").asText();
                } else if (response.statusCode() == 429) {
                    log.warn("Rate limited by OpenAI. Attempt {} of {}. Waiting before retry...", attempt, maxRetries);
                    Thread.sleep(1000L * attempt);
                } else {
                    log.error("OpenAI request failed with status: {}. Body: {}", response.statusCode(), response.body());
                    throw new RuntimeException("OpenAI API returned status code " + response.statusCode());
                }
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} to contact OpenAI failed: {}", attempt, e.getMessage());
                try {
                    Thread.sleep(500L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.error("Failed to call OpenAI after {} retries. Fallback to mock.", maxRetries, lastException);
        return mockResponse(userPrompt);
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    @Override
    public String getModelName() {
        return model;
    }

    private String mockResponse(String userPrompt) {
        // Fallback generator for demo if API key isn't set, providing a realistic mockup JSON structure.
        String lowercasePrompt = userPrompt.toLowerCase();
        int score = 10;
        String level = "LOW";
        String category = "UNKNOWN";
        String summary = "This message appears to be safe and informational.";
        String recommendation = "No action required. Normal parameters.";
        List<String> indicators = new ArrayList<>();

        if (lowercasePrompt.contains("otp") || lowercasePrompt.contains("verification code")) {
            score = 75;
            level = "HIGH";
            category = "OTP_SCAM";
            summary = "The message requests a one-time verification code, which is a common account hijacking indicator.";
            recommendation = "Do NOT share your OTP with anyone. Banks and service providers will never call or message to ask for it.";
            indicators.add("OTP Request");
            indicators.add("Hijacking Risk");
        } else if (lowercasePrompt.contains("kyc") || lowercasePrompt.contains("verify") || lowercasePrompt.contains("pan")) {
            score = 85;
            level = "HIGH";
            category = "KYC_SCAM";
            summary = "The message mimics a KYC deadline threat to siphon credential details or identity documents.";
            recommendation = "Verify status directly with your branch. Do not click links to update KYC details.";
            indicators.add("Urgency");
            indicators.add("KYC Threat");
            indicators.add("Document Request");
        } else if (lowercasePrompt.contains("suspend") || lowercasePrompt.contains("blocked") || lowercasePrompt.contains("blocked")) {
            score = 90;
            level = "CRITICAL";
            category = "BANKING_FRAUD";
            summary = "Suspicious notification warning of account block, prompting immediate login credentials submission.";
            recommendation = "Do not use phone link to log in. Contact customer care via trusted numbers.";
            indicators.add("Account Threat");
            indicators.add("Urgent Action");
        }

        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("riskScore", score);
            root.put("riskLevel", level);
            root.put("category", category);
            root.put("summary", summary);
            root.put("recommendation", recommendation);
            root.set("indicators", objectMapper.valueToTree(indicators));
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return "{\"riskScore\":0,\"riskLevel\":\"LOW\",\"category\":\"UNKNOWN\",\"summary\":\"Safe\",\"indicators\":[],\"recommendation\":\"None\"}";
        }
    }
}
