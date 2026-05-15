package com.heloword.frontendapi.service.translation.impl;

import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.heloword.frontendapi.config.LLMProperties;
import com.heloword.frontendapi.model.request.TranslationAnalyzeRequest;
import com.heloword.frontendapi.service.translation.TranslationAnalyzeService;

@Log4j2
@Service
public class TranslationAnalyzeServiceImpl implements TranslationAnalyzeService {

  @Autowired
  private LLMProperties llmProperties;

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String analyze(TranslationAnalyzeRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    String system = buildSystemPrompt(langLabel);
    String userContent = buildUserContent(request);

    Map<String, Object> body = Map.of(
        "model", llmProperties.getModel(),
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
      Map<String, Object> response = restTemplate.postForObject(llmProperties.getUrl(), entity, Map.class);
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
      log.error("TranslationAnalyze AI call failed: {}", e.getMessage());
      throw new RuntimeException("AI analyze request failed: " + e.getMessage(), e);
    }
  }

  private String resolveLangLabel(String lang) {
    if ("jp".equals(lang)) return "日文";
    if ("kr".equals(lang)) return "韓文";
    return "英文";
  }

  private String buildSystemPrompt(String langLabel) {
    return "你是一位台灣" + langLabel + "老師，我是一位正在學習" + langLabel + "的學生，"
        + "請用繁體中文分析我的翻譯練習，"
        + "比較正確答案和我的輸入，指出錯誤之處及原因，並給出更好的修正建議，"
        + "回覆請控制在7句以內，簡潔有力，"
        + "如果是日文請在漢字旁加上假名（括號內），"
        + "請只用繁體中文回覆！！！";
  }

  private String buildUserContent(TranslationAnalyzeRequest request) {
    return "中文原句：" + request.getTranslation() + "\n"
        + "正確答案：" + request.getAnswer() + "\n"
        + "我的翻譯：" + request.getUserInput();
  }
}
