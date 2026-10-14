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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.frontendapi.config.LLMProperties;
import com.heloword.frontendapi.service.ai.PromptRules;
import com.heloword.frontendapi.service.vocab.PhotoParseService;

import java.time.Duration;

@Log4j2
@Service
public class PhotoParseServiceImpl implements PhotoParseService {

  public static final String MAX_WORDS_EXTRACTED = "5";

  /** Prefer meanings written in the photo (e.g. by a teacher on a whiteboard) over the model's own translation. */
  private static final String USE_VISIBLE_MEANINGS =
      "IMPORTANT: If the image already shows a meaning or translation written next to a word "
      + "(e.g. a teacher's notes on a whiteboard), copy that meaning exactly as written into the matching field "
      + "(Chinese meaning -> \"translateCh\", English meaning -> \"translateEn\"); do NOT re-translate, rephrase, shorten or 'correct' it. "
      + "Only translate yourself when no meaning for that word is visible. "
      + "If only the Chinese meaning is visible, base \"translateEn\" on that Chinese meaning so both match the teacher's intent. ";

  @Autowired
  private LLMProperties llmProperties;

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
          "model", llmProperties.getModel(),
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
      Map<String, Object> response = restTemplate.postForObject(llmProperties.getUrl(), entity, Map.class);
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
          + PromptRules.FURIGANA_EN + " If readings are written in the image, use those readings. "
          + PromptRules.TRADITIONAL_EN + " "
          + USE_VISIBLE_MEANINGS
          + "Return ONLY a JSON array, no markdown. Each object: "
          + "\"word\" (Japanese word/phrase), "
          + "\"translateEn\" (English meaning: as written in the image if visible, otherwise your translation), "
          + "\"translateCh\" (Traditional Chinese meaning: as written in the image if visible, otherwise your translation), "
          + "\"phonetics\" (furigana if visible, otherwise empty string), "
          + "\"sentence\" (example sentence if visible, otherwise empty string). "
          + "Example (image shows 「日本へ行く　前往日本」, so the written meaning is copied as-is): "
          + "[{\"word\":\"日本[にほん]へ行[い]く\",\"translateEn\":\"go to Japan\",\"translateCh\":\"前往日本\",\"phonetics\":\"\",\"sentence\":\"\"}]";
    } else if ("ZH".equals(normalizedLang)) {
      return "Extract at max " + MAX_WORDS_EXTRACTED + " Chinese words or phrases from this image. "
          + PromptRules.TRADITIONAL_EN + " If the image contains Simplified Chinese, convert it to Traditional Chinese. "
          + "Return ONLY a JSON array, no markdown. Each object: "
          + "\"word\" (Chinese word/phrase), "
          + "\"translateEn\" (English meaning), "
          + "\"translateCh\" (same as word), "
          + "\"phonetics\" (zhuyin or pinyin if visible, otherwise empty string), "
          + "\"sentence\" (example sentence if visible, otherwise empty string). "
          + "Example: [{\"word\":\"蘋果\",\"translateEn\":\"apple\",\"translateCh\":\"蘋果\",\"phonetics\":\"\",\"sentence\":\"\"}]";
    } else {
      return "Extract at max " + MAX_WORDS_EXTRACTED + " English words or phrases from this image. "
          + PromptRules.TRADITIONAL_EN + " "
          + USE_VISIBLE_MEANINGS
          + "Return ONLY a JSON array, no markdown. Each object: "
          + "\"word\" (English word/phrase), "
          + "\"translateEn\" (definition in English: as written in the image if visible, otherwise your own), "
          + "\"translateCh\" (Traditional Chinese meaning: as written in the image if visible, otherwise your translation), "
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
      if (content instanceof String) return PromptRules.normalizeFurigana((String) content);
    }
    Object resp = response.get("response");
    if (resp instanceof String) return PromptRules.normalizeFurigana((String) resp);
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
