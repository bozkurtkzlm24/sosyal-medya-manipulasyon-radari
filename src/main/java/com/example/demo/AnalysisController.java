package com.example.demo;

import com.example.demo.dto.AnalysisRequest;
import com.example.demo.dto.AnalysisResponse;
import com.example.demo.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    // Servisimizi (beyni) içeri alıyoruz (Dependency Injection)
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // Artık GET değil, gövdesinde veri taşıyan güvenli POST isteği karşılıyoruz
    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalysisRequest request) {
        AnalysisResponse response = analysisService.analyze(request);
        return ResponseEntity.ok(response);
    }
}