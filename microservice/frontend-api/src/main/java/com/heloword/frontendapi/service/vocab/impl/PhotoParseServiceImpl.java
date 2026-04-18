package com.heloword.frontendapi.service.vocab.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.frontendapi.service.vocab.PhotoParseService;

import java.time.Duration;

@Log4j2
@Service
public class PhotoParseServiceImpl implements PhotoParseService {

  private static final String LLM_URL = "https://tunnel.heloword.com/api/chat";
  private static final String MODEL = "gemma4:26b-a4b-it-q4_K_M";
    public static final String MAX_WORDS_EXTRACTED = "5";

    private final RestTemplate restTemplate = new RestTemplateBuilder()
      .setReadTimeout(Duration.ofSeconds(30))
      .build();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<UserCustomWordDto> parseWordsFromPhoto(MultipartFile image, String lang) {
    try {
      byte[] bytes = image.getBytes();
      String base64 = Base64.getEncoder().encodeToString(bytes);
      String prompt = buildPrompt(lang);

      Map<String, Object> body = Map.of(
          "model", MODEL,
          "messages", List.of(
              Map.of(
                  "role", "user",
                  "content", prompt,
                  "images", List.of(base64)
              )
          ),
          "think", false,
          "temperature", 0,
          "stream", false
      );

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(List.of(MediaType.APPLICATION_JSON));

      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.postForObject(LLM_URL, entity, Map.class);
      if (response == null) return List.of();

      String content = extractContent(response);
      if (content == null || content.isBlank()) return List.of();

      return parseJsonToWords(content);
    } catch (Exception e) {
      log.error("Photo parse failed: {}", e.getMessage());
      throw new RuntimeException("Failed to parse photo: " + e.getMessage(), e);
    }
  }

  private String buildPrompt(String lang) {
    String normalizedLang = lang == null ? "EN" : lang.toUpperCase();
    if ("JA".equals(normalizedLang)) {
      return "Extract at max " + MAX_WORDS_EXTRACTED + " Japanese words or phrases from this image. "
          + "Return ONLY a JSON array, no markdown. Each object: "
          + "\"word\" (Japanese word/phrase), "
          + "\"translateEn\" (English meaning), "
          + "\"translateCh\" (Traditional Chinese, empty string if unknown), "
          + "\"phonetics\" (furigana if visible, otherwise empty string), "
          + "\"sentence\" (example sentence if visible, otherwise empty string). "
          + "Example: [{\"word\":\"猫\",\"translateEn\":\"cat\",\"translateCh\":\"貓\",\"phonetics\":\"ねこ\",\"sentence\":\"\"}]";
    } else if ("ZH".equals(normalizedLang)) {
      return "Extract at max " + MAX_WORDS_EXTRACTED + " Traditional Chinese words or phrases from this image. "
          + "Return ONLY a JSON array, no markdown. Each object: "
          + "\"word\" (Chinese word/phrase), "
          + "\"translateEn\" (English meaning), "
          + "\"translateCh\" (same as word), "
          + "\"phonetics\" (zhuyin or pinyin if visible, otherwise empty string), "
          + "\"sentence\" (example sentence if visible, otherwise empty string). "
          + "Example: [{\"word\":\"蘋果\",\"translateEn\":\"apple\",\"translateCh\":\"蘋果\",\"phonetics\":\"\",\"sentence\":\"\"}]";
    } else {
      return "Extract at max " + MAX_WORDS_EXTRACTED + " English words or phrases from this image. "
          + "Return ONLY a JSON array, no markdown. Each object: "
          + "\"word\" (English word/phrase), "
          + "\"translateEn\" (definition in English), "
          + "\"translateCh\" (Traditional Chinese translation), "
          + "\"phonetics\" (pronunciation if visible, otherwise empty string), "
          + "\"sentence\" (example sentence if visible, otherwise empty string). "
          + "Example: [{\"word\":\"apple\",\"translateEn\":\"a round fruit\",\"translateCh\":\"蘋果\",\"phonetics\":\"\",\"sentence\":\"\"}]";
    }
  }

  @SuppressWarnings("unchecked")
  private String extractContent(Map<String, Object> response) {
    Object message = response.get("message");
    if (message instanceof Map) {
      Object content = ((Map<String, Object>) message).get("content");
      if (content instanceof String) return (String) content;
    }
    Object resp = response.get("response");
    if (resp instanceof String) return (String) resp;
    return null;
  }

  private List<UserCustomWordDto> parseJsonToWords(String content) {
    String json = extractJsonArray(content);
    if (json == null) {
      log.warn("No JSON array found in LLM response: {}", content);
      return List.of();
    }
    try {
      List<Map<String, Object>> items = objectMapper.readValue(json, new TypeReference<>() {});
      List<UserCustomWordDto> result = new ArrayList<>();
      for (Map<String, Object> item : items) {
        String word = getString(item, "word");
        if (word == null || word.isBlank()) continue;
        UserCustomWordDto dto = new UserCustomWordDto();
        dto.setWord(word.trim());
        dto.setTranslateEn(getString(item, "translateEn", ""));
        dto.setTranslateCh(getString(item, "translateCh", ""));
        dto.setSentence(getString(item, "sentence", ""));
        dto.setPhonetics(getString(item, "phonetics", ""));
        result.add(dto);
      }
      return result;
    } catch (Exception e) {
      log.warn("Failed to parse JSON from LLM: {}", e.getMessage());
      return List.of();
    }
  }

  private String extractJsonArray(String content) {
    int start = content.indexOf('[');
    if (start < 0) return null;
    // Re-append ']' in case the stop sequence consumed it before emission
    String trimmed = content.substring(start).stripTrailing();
    if (!trimmed.endsWith("]")) trimmed = trimmed + "]";
    int end = trimmed.lastIndexOf(']');
    return trimmed.substring(0, end + 1);
  }

  private String getString(Map<String, Object> map, String key) {
    Object val = map.get(key);
    return val instanceof String ? ((String) val).trim() : null;
  }

  private String getString(Map<String, Object> map, String key, String defaultVal) {
    String val = getString(map, key);
    return val != null ? val : defaultVal;
  }
}
