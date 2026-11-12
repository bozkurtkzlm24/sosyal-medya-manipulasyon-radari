package com.example.demo.service;

import com.example.demo.NlpService;
import com.example.demo.dto.AiAnalysisResult;
import com.example.demo.dto.AnalysisRequest;
import com.example.demo.dto.AnalysisResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AnalysisService {

    private final NlpService nlpService;
    private final TwitterScraperService twitterScraperService;
    private final AiService aiService;

    // 1. Şüpheli / Troll / Provokasyon Kökleri
    private final List<String> highRiskKeywords = Arrays.asList(
            "ihanet", "vatan", "hain", "kumpas", "talan", "darbe", "ayaklanma", "susturamazsınız", "satılmış"
    );

    // 2. Kışkırtma ve Tıklama Tuzağı Kökleri
    private final List<String> clickbaitKeywords = Arrays.asList(
            "sok", "flas", "acil", "bakin", "gizlenen", "paylas", "yayalim", "herkes", "duysun", "gorun"
    );

    // 3. Tekzip, Doğrulama ve Sağduyu İfadeleri (Riski düşüren koruyucu kalıplar)
    private final List<String> debunkPatterns = Arrays.asList(
            "yalandır", "yalanlama", "asılsızdır", "asılsız", "doğru değil", "dogru degil",
            "inanmayın", "inanmayin", "provokasyona gelmeyin", "algı operasyonu", "iftiradır",
            "teyit edildi", "yalan haber", "gerçeği yansıtmıyor"
    );

    public AnalysisService(NlpService nlpService,
                           TwitterScraperService twitterScraperService,
                           AiService aiService) {
        this.nlpService = nlpService;
        this.twitterScraperService = twitterScraperService;
        this.aiService = aiService;
    }

    public AnalysisResponse analyze(AnalysisRequest request) {
        String rawInput = request.getText() != null ? request.getText().trim() : "";
        if (rawInput.isEmpty()) {
            throw new IllegalArgumentException("Lütfen analiz edilecek bir tweet veya link yapıştırın.");
        }

        String targetText = rawInput;

        // Link kontrolü: Eğer X linkiyse içeriğini çek
        if (twitterScraperService.isTweetUrl(rawInput)) {
            String extractedText = twitterScraperService.fetchTweetText(rawInput);
            if (extractedText != null && !extractedText.isBlank()) {
                targetText = extractedText;
                System.out.println(">>> [TWITTER'DAN ÇEKİLEN METİN]: " + targetText);
            }
        }

        AnalysisResponse response = new AnalysisResponse();
        response.setAnalyzedText(targetText);

        // 1. Dilbilimsel Anomali (Büyük harf ve ünlem histerisi)
        double anomalyScore = calculateLinguisticAnomaly(targetText);
        response.setLinguisticAnomalyScore(anomalyScore);

        // 2. Metin Temizliği ve Kök Analizi
        String clean = nlpService.metniTemizle(targetText);
        List<String> stems = nlpService.kokleriBul(clean);

        double textScore = 0.0;
        List<String> flagged = new ArrayList<>();

        for (String stem : stems) {
            if (highRiskKeywords.contains(stem)) {
                textScore += 35.0;
                flagged.add(stem);
            } else if (clickbaitKeywords.contains(stem)) {
                textScore += 20.0;
                flagged.add(stem);
            }
        }
        response.setFlaggedKeywords(flagged);

        // 3. Tekzip / Yalanlama Kontrolü
        boolean isDebunking = checkIfDebunking(targetText.toLowerCase());

        // 4. Hesap Verisi Kontrolü
        boolean hasAccountData = request.getAccountAgeDays() > 0 ||
                request.getFollowerCount() > 0 ||
                request.getFollowingCount() > 0;

        double finalScore;
        if (hasAccountData) {
            double botScore = calculateBotProbability(
                    request.getAccountAgeDays(),
                    request.getFollowerCount(),
                    request.getFollowingCount()
            );
            response.setBotProbability(botScore);
            finalScore = (textScore * 0.50) + (botScore * 0.30) + (anomalyScore * 0.20);
        } else {
            response.setBotProbability(0.0);
            finalScore = (textScore * 0.70) + (anomalyScore * 0.30);
        }

        // Tekzip / Yalanlama İndirimi
        if (isDebunking) {
            finalScore = finalScore * 0.30;
        }

        // 5. BERTurk / AI Modeli Entegrasyonu (Hibrit Karar)
        AiAnalysisResult aiResult = aiService.predictManipulation(targetText);
        double roundedConfidence = Math.round(aiResult.getConfidence() * 100.0);

        // Arayüzde rozet olarak göstermek için AI verilerini cevaba ekliyoruz
        response.setAiLabel(aiResult.getLabel());
        response.setAiConfidence(roundedConfidence);

        if ("ORGANIC_FACTCHECK".equals(aiResult.getLabel())) {
            finalScore = finalScore * 0.20; // AI teyit/haber diyorsa riski ciddi oranda düşür
        } else if ("POTENTIAL_PROPAGANDA".equals(aiResult.getLabel())) {
            finalScore = Math.max(finalScore, roundedConfidence); // AI güçlü manipülasyon görüyorsa taban puanı yükselt
        }

        finalScore = Math.min(Math.max(finalScore, 0.0), 100.0);
        response.setTotalRiskScore(Math.round(finalScore * 10.0) / 10.0);

        // Seviye Belirleme
        if (isDebunking && finalScore < 30.0) {
            response.setRiskLevel("BİLGİLENDİRME / TEKZİP (Güvenli)");
        } else if (finalScore >= 70.0) {
            response.setRiskLevel("YÜKSEK RİSK (Manipülasyon / Troll)");
        } else if (finalScore >= 40.0) {
            response.setRiskLevel("ORTA RİSK (Şüpheli / Clickbait)");
        } else {
            response.setRiskLevel("GÜVENLİ (Organik İçerik)");
        }

        return response;
    }

    private boolean checkIfDebunking(String lowerText) {
        for (String pattern : debunkPatterns) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private double calculateLinguisticAnomaly(String text) {
        if (text.length() < 5) return 0.0;

        double anomaly = 0.0;
        long uppercaseCount = text.chars().filter(Character::isUpperCase).count();
        double uppercaseRatio = (double) uppercaseCount / text.length();
        if (uppercaseRatio > 0.35) anomaly += 40.0;

        if (text.contains("!!!") || text.contains("!?!") || text.contains("???")) anomaly += 30.0;

        long mentionCount = Arrays.stream(text.split(" ")).filter(w -> w.startsWith("@")).count();
        if (mentionCount >= 3) anomaly += 20.0;

        return Math.min(anomaly, 100.0);
    }

    private double calculateBotProbability(int ageDays, int followers, int following) {
        double score = 0.0;
        if (ageDays > 0 && ageDays < 30) score += 35.0;
        if (following > 1000 && followers < 50) score += 45.0;
        if (following > 0 && ((double) followers / following) < 0.05) score += 20.0;
        return Math.min(score, 100.0);
    }
}