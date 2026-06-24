package com.scamshield.qr.service.impl;

import com.scamshield.qr.service.MerchantQRParser;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MerchantQRParserImpl implements MerchantQRParser {

    private static final Pattern PN_PATTERN = Pattern.compile("[?&]pn=([^&]+)");

    @Override
    public String extractMerchantName(String rawText) {
        if (rawText == null) {
            return "Unknown Merchant";
        }

        try {
            Matcher matcher = PN_PATTERN.matcher(rawText);
            if (matcher.find()) {
                return URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // Fallback
        }
        return "Individual Account";
    }
}
