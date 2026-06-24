package com.scamshield.unit.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.ai.provider.OpenAIProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAIProviderTest {

    private OpenAIProvider openAiProvider;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        openAiProvider = new OpenAIProvider("test-api-key", "gpt-4o-mini", objectMapper);
        ReflectionTestUtils.setField(openAiProvider, "httpClient", mockHttpClient);
    }

    @Test
    void executePrompt_withValidApiKeyAndSuccessfulResponse_shouldReturnAiOutput() throws IOException, InterruptedException {
        String expectedContent = "{\"riskScore\": 85, \"riskLevel\": \"HIGH\", \"category\": \"KYC_SCAM\"}";
        String responseBody = "{\"choices\":[{\"message\":{\"content\":\"" + expectedContent.replace("\"", "\\\"") + "\"}}]}";

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(responseBody);
        
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        String result = openAiProvider.executePrompt("System Prompt", "User Prompt");
        assertEquals(expectedContent, result);
    }

    @Test
    void executePrompt_whenRateLimited_shouldRetryAndSucceed() throws IOException, InterruptedException {
        String expectedContent = "{\"riskScore\": 10}";
        String responseBody = "{\"choices\":[{\"message\":{\"content\":\"" + expectedContent.replace("\"", "\\\"") + "\"}}]}";

        // First attempt returns 429, second attempt succeeds with 200
        when(mockHttpResponse.statusCode()).thenReturn(429, 200);
        when(mockHttpResponse.body()).thenReturn("", responseBody);

        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        String result = openAiProvider.executePrompt("System Prompt", "User Prompt");
        assertEquals(expectedContent, result);
        
        // Verify send was called twice
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void executePrompt_whenEmptyApiKey_shouldReturnMockResponse() {
        // Create a provider with empty API key
        OpenAIProvider fallbackProvider = new OpenAIProvider("", "gpt-4o-mini", objectMapper);

        String result = fallbackProvider.executePrompt("System Prompt", "Please verify your kyc details immediately.");
        
        assertTrue(result.contains("KYC_SCAM"));
        assertTrue(result.contains("riskScore"));
    }
}
