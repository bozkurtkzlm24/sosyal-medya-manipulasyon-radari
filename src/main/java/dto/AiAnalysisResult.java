package com.example.demo.dto;

public class AiAnalysisResult {
    private String label;        // Örn: MANIPULATION, NEUTRAL, DEBUNK
    private double confidence;   // 0.0 ile 1.0 arası güven skoru (Örn: 0.92)

    public AiAnalysisResult() {
    }

    public AiAnalysisResult(String label, double confidence) {
        this.label = label;
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}