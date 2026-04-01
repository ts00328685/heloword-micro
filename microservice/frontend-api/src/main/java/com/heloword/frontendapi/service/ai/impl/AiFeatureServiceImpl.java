package com.heloword.frontendapi.service.ai.impl;

import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.service.ai.AiFeatureService;

@Log4j2
@Service
public class AiFeatureServiceImpl implements AiFeatureService {

  private static final String LLM_URL = "https://tunnel.heloword.com/api/chat";
  private static final String MODEL = "qwen3:1.7b";

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String wordInsight(WordInsightRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";
    String ch = request.getTranslateCh() != null ? request.getTranslateCh() : "";

    String system = "你是一位" + langLabel + "老師。請嚴格按照以下格式回覆，不要增減任何欄位：\n"
        + "解釋：（用繁體中文簡短解釋單字的意思，1-2句）\n"
        + langLabel + "例句：（用" + langLabel + "造一個自然的例句）\n"
        + "中文翻譯：（將上面例句翻譯成繁體中文）";
    String user = "單字：" + word
        + (en.isEmpty() ? "" : "（英文意思：" + en + "）")
        + (ch.isEmpty() ? "" : "（中文意思：" + ch + "）");

    return callLlm(system, user);
  }

  @Override
  public String sampleSentence(SampleSentenceRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";

    String system = "你是一位" + langLabel + "老師。請嚴格按照以下格式回覆，不要增減任何欄位：\n"
        + langLabel + "例句：（用" + langLabel + "造一個自然的例句）\n"
        + "中文翻譯：（將上面例句翻譯成繁體中文）";
    String user = "單字：" + word + (en.isEmpty() ? "" : "（意思：" + en + "）");

    return callLlm(system, user);
  }

  @Override
  public String studyCoach(StudyCoachRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    int pct = request.getAccuracyPct();
    int wrong = request.getWrongCount();

    String system = "你是一位鼓勵學生的" + langLabel + "學習教練。請用繁體中文回覆，給出一句鼓勵的話和一個具體的學習建議，共2句，不要其他內容。";
    String user = "我最近的背單字正確率是" + pct + "%，答錯了" + wrong + "個單字。";

    return callLlm(system, user);
  }

  private String callLlm(String system, String userContent) {
    Map<String, Object> body = Map.of(
        "model", MODEL,
        "messages", List.of(
            Map.of("role", "system", "content", system),
            Map.of("role", "user", "content", userContent)
        ),
        "think", false,
        "temperature", 0,
        "stream", false
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    try {
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.postForObject(LLM_URL, entity, Map.class);
      if (response == null) return "查無內容";

      Object message = response.get("message");
      if (message instanceof Map) {
        @SuppressWarnings("unchecked")
        Object content = ((Map<String, Object>) message).get("content");
        if (content instanceof String) return (String) content;
      }
      Object resp = response.get("response");
      if (resp instanceof String) return (String) resp;

      return "查無內容";
    } catch (Exception e) {
      log.error("AI feature call failed: {}", e.getMessage());
      throw new RuntimeException("AI request failed: " + e.getMessage(), e);
    }
  }

  /** Resolves UI language code (i18n) to a display label. */
  private String resolveLangLabel(String lang) {
    if ("jp".equals(lang)) return "日文";
    if ("kr".equals(lang)) return "韓文";
    return "英文";
  }

  /** Resolves the word's own language code to a display label used in prompts. */
  private String resolveWordLangLabel(String wordLang) {
    if (wordLang == null) return "英文";
    if ("jp".equals(wordLang)) return "日文";
    if ("de".equals(wordLang)) return "德文";
    if ("kr".equals(wordLang)) return "韓文";
    if ("ch".equals(wordLang)) return "中文";
    return "英文";
  }
}
