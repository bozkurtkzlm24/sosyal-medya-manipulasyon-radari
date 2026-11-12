package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    // 1 IP adresi için 1 dakikada izin verilen maksimum istek sayısı:
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    // IP adreslerini ve istek sayılarını hafızada güvenle tutan harita:
    private final Map<String, RequestTracker> ipTrackers = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Yalnızca analiz API'mizi koruma altına alıyoruz (HTML veya JS dosyalarını engellemiyoruz)
        if (path.startsWith("/api/v1/analysis")) {
            String clientIp = getClientIp(request);
            long currentTime = System.currentTimeMillis();

            // IP için kayıt oluştur veya mevcut kaydı al
            RequestTracker tracker = ipTrackers.computeIfAbsent(clientIp, k -> new RequestTracker(currentTime));

            // 1 dakika (60.000 milisaniye) dolduysa sayacı sıfırla
            if (currentTime - tracker.startTime > 60_000) {
                tracker.startTime = currentTime;
                tracker.count.set(0);
            }

            // İstek sayısını 1 artır
            int currentRequests = tracker.count.incrementAndGet();

            // Sınır aşıldıysa kapıyı kapat, 429 Too Many Requests dön
            if (currentRequests > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"status\": 429, \"error\": \"Çok Fazla İstek\", " +
                                "\"message\": \"Dakikalık istek limitine ulaştınız (Maksimum 10 istek). Lütfen 1 dakika bekleyin.\"}"
                );
                return; // İsteğin arkadaki Controller'a gitmesini engeller!
            }
        }

        // Limit aşılmadıysa isteğin geçmesine izin ver
        filterChain.doFilter(request, response);
    }

    // Gerçek IP adresini alan yardımcı metot
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    // IP'nin başlangıç zamanını ve istek sayacını tutan yardımcı sınıf
    private static class RequestTracker {
        long startTime;
        AtomicInteger count;

        RequestTracker(long startTime) {
            this.startTime = startTime;
            this.count = new AtomicInteger(0);
        }
    }
}