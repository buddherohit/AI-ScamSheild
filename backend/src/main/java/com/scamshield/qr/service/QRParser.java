package com.scamshield.qr.service;

public interface QRParser {
    String parse(byte[] qrImageBytes) throws QrParsingException;
}
