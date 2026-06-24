package com.scamshield.ai.service;

import com.scamshield.ai.entity.PromptTemplate;
import com.scamshield.ai.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptEngineeringService {

    private final PromptTemplateRepository promptTemplateRepository;

    public String buildPrompt(String templateKey, Map<String, Object> parameters) {
        PromptTemplate template = promptTemplateRepository.findByTemplateKey(templateKey)
                .orElseThrow(() -> new IllegalArgumentException("Prompt template not found: " + templateKey));

        String value = template.getTemplateValue();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String replacement = entry.getValue() != null ? entry.getValue().toString() : "";
            value = value.replace(placeholder, replacement);
        }
        return value;
    }
}
