package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TwitterScraperService {

    private final RestTemplate restTemplate;

    public TwitterScraperService() {
        // Doğrudan Spring'in standart RestTemplate nesnesini başlatıyoruz
        this.restTemplate = new RestTemplate();
    }

    // Gelen metin bir twitter/x linki mi kontrol eder
    public boolean isTweetUrl(String text) {
        if (text == null) return false;
        String trimmed = text.trim();
        return trimmed.startsWith("http") && (trimmed.contains("twitter.com") || trimmed.contains("x.com"));
    }

    // Linkin içine girip tweet'in metnini çeker
    @SuppressWarnings("unchecked")
    public String fetchTweetText(String tweetUrl) {
        try {
            // Twitter OEMBED resmi açık servisi
            String oembedUrl = "https://publish.twitter.com/oembed?url=" + tweetUrl.trim();
            Map<String, Object> response = restTemplate.getForObject(new URI(oembedUrl), Map.class);

            if (response != null && response.containsKey("html")) {
                String htmlContent = (String) response.get("html");
                return stripHtmlTags(htmlContent);
            }
        } catch (Exception e) {
            System.err.println("Tweet çekilirken hata oluştu: " + e.getMessage());
        }
        return null;
    }

    private String stripHtmlTags(String html) {
        Pattern pattern = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        String text = html;

        if (matcher.find()) {
            text = matcher.group(1);
        }

        // Tüm HTML etiketlerini (<a..>, <br>) temizle ve HTML özel karakterlerini düzelt
        return text.replaceAll("<[^>]*>", " ")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&mdash;", "-")
                .replaceAll("\\s+", " ")
                .trim();
    }
}