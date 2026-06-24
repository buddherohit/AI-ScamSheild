package com.scamshield.ai.provider;

public interface AIProvider {
    String executePrompt(String systemPrompt, String userPrompt);
    String getProviderName();
    String getModelName();
}
