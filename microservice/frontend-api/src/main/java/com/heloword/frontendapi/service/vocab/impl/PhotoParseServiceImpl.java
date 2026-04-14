package com.heloword.frontendapi.service.vocab.impl;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.frontendapi.service.vocab.PhotoParseService;

@Log4j2
@Service
public class PhotoParseServiceImpl implements PhotoParseService {

  private static final String LLM_URL = "http://localhost:11434/api/chat";
  private static final String MODEL = "gemma4:e4b";

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public List<UserCustomWordDto> parseWordsFromPhoto(MultipartFile image) {
    try {
      byte[] bytes = image.getBytes();
      String base64 = Base64.getEncoder().encodeToString(bytes);

      String prompt = "You are a vocabulary extraction assistant. "
          + "Extract ALL vocabulary words or phrases visible in this image. "
          + "Return ONLY a valid JSON array (no markdown, no extra text) where each object has exactly these fields: "
          + "\"word\" (the word or phrase, required), "
          + "\"translateEn\" (English meaning or definition, required — provide one even if not in the image), "
          + "\"translateCh\" (Chinese translation, empty string if not visible), "
          + "\"sentence\" (example sentence if visible in the image, otherwise empty string), "
          + "\"phonetics\" (pronunciation guide if visible in the image, otherwise empty string). "
          + "Example: [{\"word\":\"apple\",\"translateEn\":\"a round fruit\","
          + "\"translateCh\":\"蘋果\",\"sentence\":\"\",\"phonetics\":\"\"}]";

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

  /** Extract the first [...] array from a string, handling markdown code fences. */
  private String extractJsonArray(String content) {
    int start = content.indexOf('[');
    int end = content.lastIndexOf(']');
    if (start >= 0 && end > start) {
      return content.substring(start, end + 1);
    }
    return null;
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
