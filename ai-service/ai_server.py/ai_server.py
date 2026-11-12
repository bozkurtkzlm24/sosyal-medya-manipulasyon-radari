from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

app = FastAPI(title="BERTurk Manipülasyon Analiz Servisi")

# Hugging Face üzerinden Türkçe eğitilmiş BERTurk duygu ve ton analiz modelini yüklüyoruz
# Model ilk çalıştırmada otomatik olarak indirilecek (~400 MB) ve belleğe alınacaktır.
MODEL_NAME = "savasy/bert-base-turkish-sentiment-cased"
classifier = pipeline("sentiment-analysis", model=MODEL_NAME)

class AnalysisRequest(BaseModel):
    text: str

class AnalysisResponse(BaseModel):
    label: str
    confidence: float

@app.post("/predict", response_model=AnalysisResponse)
def predict(request: AnalysisRequest):
    text = request.text.strip()
    if not text:
        return AnalysisResponse(label="NEUTRAL", confidence=0.50)

    # BERTurk modeline metni verip tahmin alıyoruz
    result = classifier(text)[0]

    # Modelin çıktısı: 'LABEL_0' (Negatif/Öfke/Saldırgan) veya 'LABEL_1' (Pozitif/Ilımlı/Organik)
    raw_label = result['label']
    score = round(float(result['score']), 2)

    # Bizim Spring Boot projemizin anladığı etiketlere çeviriyoruz
    if raw_label in ["LABEL_0", "negative", "negative "]:
        # Yüksek öfke, kutuplaştırma veya hedef gösterme dili
        mapped_label = "POTENTIAL_PROPAGANDA"
    else:
        # Ilımlı, nötr veya organik açıklama
        mapped_label = "ORGANIC_FACTCHECK"

    return AnalysisResponse(label=mapped_label, confidence=score)

if __name__ == "__main__":
    import uvicorn
    # Docker konteyner dışından istek alabilmesi için 0.0.0.0 yapıyoruz
    uvicorn.run(app, host="0.0.0.0", port=8000)