# 🛡️ Sosyal Medya Manipülasyon ve Bot Tespit Radarı

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2F%204.x-brightgreen?logo=springboot)
![Python](https://img.shields.io/badge/Python-3.11-blue?logo=python)
![FastAPI](https://img.shields.io/badge/FastAPI-0.110+-009688?logo=fastapi)
![HuggingFace](https://img.shields.io/badge/NLP-BERTurk-yellow?logo=huggingface)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

Türkçe sosyal medya paylaşımlarında (özellikle X/Twitter içeriklerinde) **manipülasyon, troll kampanyaları, tıklama tuzağı (clickbait) ve yapay öfke dilini** tespit etmek için geliştirilmiş hibrit bir mikroservis uygulamasıdır.

Kural tabanlı Türkçe morfolojik doğal dil işleme (NLP) motorunu, **BERTurk** derin öğrenme modeliyle birleştirerek yalnızca kelimelere değil, cümlenin bağlamına (örneğin bir yalanı yayma mı yoksa tekzip/yalanlama mı olduğuna) göre risk skoru üretir.

---

## 🏗️ Sistem Mimarisi

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