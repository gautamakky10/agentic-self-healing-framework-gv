package com.selfhealing;

public class RecoveryResult {
    private final String locator;
    private final double confidence;

    public RecoveryResult(String locator, double confidence) {
        this.locator = locator;
        this.confidence = confidence;
    }

    public String getLocator() { return locator; }
    public double getConfidence() { return confidence; }
}
