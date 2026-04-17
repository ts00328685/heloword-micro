package com.heloword.frontendapi.service.scramble.impl;

import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.heloword.frontendapi.model.request.ScrambleAiRequest;
import com.heloword.frontendapi.service.scramble.ScrambleFrontendService;

@Log4j2
@Service
public class ScrambleFrontendServiceImpl implements ScrambleFrontendService {

  private static final String LLM_URL = "https://tunnel.heloword.com/api/chat";
  private static final String MODEL = "gemma4:26b-a4b-it-q4_K_M";

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String aiExplain(ScrambleAiRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    String system = buildSystemPrompt(langLabel);
    String userContent = request.getSentence() + "（中文翻譯：" + request.getTranslation() + "）";

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

      // Try message.content path first (Ollama chat format)
      Object message = response.get("message");
      if (message instanceof Map) {
        @SuppressWarnings("unchecked")
        Object content = ((Map<String, Object>) message).get("content");
        if (content instanceof String) return (String) content;
      }
      // Fallback to response field (older Ollama generate format)
      Object resp = response.get("response");
      if (resp instanceof String) return (String) resp;

      return "查無內容";
    } catch (Exception e) {
      log.error("ScrambleAI call failed: {}", e.getMessage());
      throw new RuntimeException("AI explain request failed: " + e.getMessage(), e);
    }
  }

  private String resolveLangLabel(String lang) {
    if ("jp".equals(lang)) return "日文";
    if ("kr".equals(lang)) return "韓文";
    return "英文";
  }

  private String buildSystemPrompt(String langLabel) {
    return "你是一位台灣" + langLabel + "老師，我是一位正在學習" + langLabel + "的學生，"
        + "請用簡短的繁體中文bullet point解釋這個" + langLabel + "句子的字詞搭配及文法重點，"
        + "請用繁體中文回覆，並參考中文給出各個字詞搭配的翻譯及句型文法重點，"
        + "please provide hiragana aside kanji in parenthesis in your answer if it's japanese "
        + "and reply with Traditional Chinese only!!!";
  }
}
