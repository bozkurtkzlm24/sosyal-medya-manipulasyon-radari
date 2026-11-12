package com.example.demo.dto;

public class AnalysisRequest {

    // Kullanıcının bize göndereceği 4 temel bilgi:
    private String text;             // Tweet veya paylaşım metni
    private int accountAgeDays;       // Hesabın yaşı (kaç günlük hesap?)
    private int followerCount;        // Takipçi sayısı
    private int followingCount;       // Takip ettiği kişi sayısı

    // Boş yapıcı metot (Spring Boot'un zarfı açabilmesi için şarttır)
    public AnalysisRequest() {
    }

    // Bilgileri okuma (get) ve içine yazma (set) metotları:
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(int accountAgeDays) {
        this.accountAgeDays = accountAgeDays;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }
}