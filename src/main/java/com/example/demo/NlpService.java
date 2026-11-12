package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NlpService {

    // Türkçe yaygın ekler (sondan başa doğru budamak için)
    private final List<String> turkceEkler = Arrays.asList(
            "lerdir", "lardır", "lerden", "lardan", "lerine", "larına",
            "siniz", "sınız", "sunuz", "sünüz", "ecek", "acak",
            "iyor", "uyor", "üyor", "ıyor", "miş", "mış", "muş", "müş",
            "dir", "dır", "dur", "dür", "tir", "tır", "tur", "tür",
            "ler", "lar", "den", "dan", "ten", "tan", "de", "da", "te", "ta",
            "ye", "ya", "le", "la", "in", "ın", "un", "ün", "i", "ı", "u", "ü"
    );

    // 1. Sosyal Medya Metin Temizliği
    public String metniTemizle(String hamMetin) {
        if (hamMetin == null) return "";

        return hamMetin
                .replaceAll("https?://\\S+\\s?", "")  // Linkleri sil
                .replaceAll("@\\w+", "")              // @etiketleri sil
                .replaceAll("[^a-zA-ZçğıöşüÇĞİÖŞÜ\\s]", " ") // Noktalama ve emojileri sil
                .replaceAll("\\s+", " ")              // Çift boşlukları teke indir
                .trim()
                .toLowerCase(Locale.forLanguageTag("tr-TR"));
    }

    // 2. Saf Java Türkçe Kök Bulucu (Stemmer)
    public List<String> kokleriBul(String temizMetin) {
        List<String> kokler = new ArrayList<>();
        if (temizMetin.isBlank()) return kokler;

        String[] kelimeler = temizMetin.split(" ");
        for (String kelime : kelimeler) {
            if (kelime.length() <= 2) {
                kokler.add(kelime);
                continue;
            }

            String kok = kelime;
            // Kelimenin sonundaki Türkçe ekleri sırayla kontrol edip temizle
            for (String ek : turkceEkler) {
                if (kok.endsWith(ek) && (kok.length() - ek.length()) >= 3) {
                    kok = kok.substring(0, kok.length() - ek.length());
                    break;
                }
            }
            kokler.add(kok);
        }
        return kokler;
    }
}