package com.example.demo.dto;

import java.util.List;

public class AnalysisResponse {

    private String analyzedText;
    private double totalRiskScore;
    private String riskLevel;
    private List<String> flaggedKeywords;
    private double botProbability;
    private double linguisticAnomalyScore;
    private String aiLabel;          // YENİ: BERTurk etiketi
    private double aiConfidence;     // YENİ: BERTurk güven yüzdesi

    public AnalysisResponse() {
    }

    public String getAnalyzedText() { return analyzedText; }
    public void setAnalyzedText(String analyzedText) { this.analyzedText = analyzedText; }

    public double getTotalRiskScore() { return totalRiskScore; }
    public void setTotalRiskScore(double totalRiskScore) { this.totalRiskScore = totalRiskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public List<String> getFlaggedKeywords() { return flaggedKeywords; }
    public void setFlaggedKeywords(List<String> flaggedKeywords) { this.flaggedKeywords = flaggedKeywords; }

    public double getBotProbability() { return botProbability; }
    public void setBotProbability(double botProbability) { this.botProbability = botProbability; }

    public double getLinguisticAnomalyScore() { return linguisticAnomalyScore; }
    public void setLinguisticAnomalyScore(double linguisticAnomalyScore) { this.linguisticAnomalyScore = linguisticAnomalyScore; }

    public String getAiLabel() { return aiLabel; }
    public void setAiLabel(String aiLabel) { this.aiLabel = aiLabel; }

    public double getAiConfidence() { return aiConfidence; }
    public void setAiConfidence(double aiConfidence) { this.aiConfidence = aiConfidence; }
}