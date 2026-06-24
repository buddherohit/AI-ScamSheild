package com.scamshield.qr.service.impl;

import com.scamshield.qr.service.UPIQRParser;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UpiQRParserImpl implements UPIQRParser {

    private static final Pattern PA_PATTERN = Pattern.compile("[?&]pa=([^&]+)");

    @Override
    public String extractUpiId(String rawText) {
        if (rawText == null) {
            return null;
        }

        // Direct UPI ID check (e.g. abc@upi)
        if (rawText.contains("@") && !rawText.contains(":") && !rawText.contains("?")) {
            return rawText.trim();
        }

        try {
            Matcher matcher = PA_PATTERN.matcher(rawText);
            if (matcher.find()) {
                return URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // Fallback
        }
        return null;
    }
}
