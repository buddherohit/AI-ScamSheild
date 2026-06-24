package com.scamshield.qr.service;

public class QrParsingException extends RuntimeException {
    public QrParsingException(String message) {
        super(message);
    }
    public QrParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
