# 🛡️ Sosyal Medya Manipülasyon ve Bot Tespit Radarı

Türkçe sosyal medya içeriklerinde (özellikle X/Twitter paylaşımlarında) **manipülasyon, troll kampanyaları, tıklama tuzağı (clickbait) ve yapay öfke dilini** tespit etmek için geliştirilmiş hibrit bir mikroservis uygulamasıdır.

Kural tabanlı morfolojik doğal dil işleme (NLP) motorunu, **BERTurk** derin öğrenme modeliyle birleştirerek yalnızca kelimelere değil, cümlenin bağlamına ve niyetine (örneğin bir yalanı yayma mı yoksa tekzip/yalanlama mı olduğuna) göre risk analizi yapar.

---

## 🏗️ Sistem Mimarisi

Uygulama, Docker ağı üzerinde izole şekilde haberleşen iki ana mikroservisten oluşur:

```text
┌──────────────────────┐      HTTP POST (JSON)      ┌──────────────────────────────┐
│   Kullanıcı / Web    │ ─────────────────────────► │   Java Spring Boot (8080)    │
│  (Bootstrap 5 UI)    │ ◄───────────────────────── │  • X/Twitter oEmbed Scraper  │
└──────────────────────┘                            │  • IP Rate Limiter (10/dk)   │
                                                    │  • Morfolojik Kök & Anomali  │
                                                    │  • Tekzip (Bağlam) Filtresi  │
                                                    └──────────────┬───────────────┘
                                                                   │
                                                      Internal Docker Network
                                                   (http://ai-service:8000/predict)
                                                                   │
                                                                   ▼
                                                    ┌──────────────────────────────┐
                                                    │    Python FastAPI (8000)     │
                                                    │  • Hugging Face Transformers │
                                                    │  • BERTurk Sentiment Model   │
                                                    └──────────────────────────────┘