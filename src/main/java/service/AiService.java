package com.example.demo.service;

import com.example.demo.dto.AiAnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
public class AiService {

    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://127.0.0.1:8000/predict}")
    private String pythonAiUrl;

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public AiAnalysisResult predictManipulation(String text) {
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var body = new HashMap();
            body.put("text", text);

            var request = new HttpEntity(body, headers);

            AiAnalysisResult response = restTemplate.postForObject(pythonAiUrl, request, AiAnalysisResult.class);

            if (response != null) {
                return response;
            }
        } catch (Exception e) {
            System.err.println(">>> [UYARI]: Python AI servisi yanıt vermedi (" + e.getMessage() + ").");
        }

        return new AiAnalysisResult("UNCERTAIN", 0.50);
    }
}