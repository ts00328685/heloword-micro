package com.heloword.frontendapi.service.scramble.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import com.heloword.frontendapi.config.CacheConfig;
import com.heloword.frontendapi.config.LLMProperties;
import com.heloword.frontendapi.model.request.ScrambleAiRequest;
import com.heloword.frontendapi.model.request.ScrambleHintRequest;
import com.heloword.frontendapi.model.request.ScrambleRecapRequest;
import com.heloword.frontendapi.model.request.ScrambleWrongOrderRequest;
import com.heloword.frontendapi.model.response.SimilarSentenceResponse;
import com.heloword.frontendapi.service.ai.PromptRules;
import com.heloword.frontendapi.service.scramble.ScrambleFrontendService;

@Log4j2
@Service
public class ScrambleFrontendServiceImpl implements ScrambleFrontendService {

  @Autowired
  private LLMProperties llmProperties;

  /** Bounded so a hung LLM can't pin request threads (and guest AI slots) forever. */
  private final RestTemplate restTemplate = new RestTemplateBuilder()
      .setConnectTimeout(Duration.ofSeconds(5))
      .setReadTimeout(Duration.ofSeconds(60))
      .build();

  @Override
  public String aiExplain(ScrambleAiRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    String system = "你是一位台灣" + langLabel + "老師，我是一位正在學習" + langLabel + "的學生，"
        + "請用簡短的繁體中文bullet point解釋這個" + langLabel + "句子的字詞搭配及文法重點，"
        + "並參考中文給出各個字詞搭配的翻譯及句型文法重點，請不要超過十句話。"
        + PromptRules.furiganaZh(request.getLang()) + PromptRules.TRADITIONAL_ZH;
    String userContent = request.getSentence() + "（中文翻譯：" + request.getTranslation() + "）";
    return chat(system, userContent, 330, 0, 768);
  }

  /**
   * Explains the learner's specific ordering mistake — not the whole sentence.
   * Cache keys include every prompt input (translation too): a shared cache keyed on
   * fewer inputs would let one client's text decide the answer other users get.
   */
  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'scr-wrong', #request.lang, #request.sentence, #request.attempt, #request.translation}")
  public String wrongOrder(ScrambleWrongOrderRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    String system = "你是一位台灣" + langLabel + "老師。學生在句子重組遊戲中把順序排錯了。"
        + "請比較「學生的順序」和「正確句子」，用繁體中文最多3個bullet point，"
        + "具體指出哪裡放錯、為什麼（說明背後的語序或文法規則，例如助詞位置、修飾語在被修飾語前、動詞的位置），"
        + "語氣簡短友善，不要逐字重寫整個句子的解析。"
        + PromptRules.furiganaZh(request.getLang()) + PromptRules.TRADITIONAL_ZH;
    String user = "正確句子：" + request.getSentence()
        + "\n學生的順序：" + request.getAttempt()
        + "\n中文翻譯：" + nullToEmpty(request.getTranslation());
    return chat(system, user, 220, 0, 1024);
  }

  /** One grammar nudge toward the next chunk, without giving the chunk away. */
  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'scr-hint', #request.lang, #request.sentence, #request.placed, #request.translation}")
  public String hint(ScrambleHintRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    String system = "你是一位台灣" + langLabel + "老師。學生正在玩句子重組，需要一點提示。"
        + "請只寫「一句」繁體中文提示（30字以內），從文法或語序的角度引導他想出「下一個」應該放的部分，"
        + "例如：「主題用は標示，通常放在句首」「時間詞常放在主詞後面」「動詞放在句尾」。"
        + "絕對不可以寫出句子裡的任何" + langLabel + "原文字詞，也不要直接說出答案。"
        + PromptRules.TRADITIONAL_ZH;
    String placed = request.getPlaced() == null || request.getPlaced().isBlank() ? "（尚未開始）" : request.getPlaced();
    String user = "正確句子：" + request.getSentence()
        + "\n中文翻譯：" + nullToEmpty(request.getTranslation())
        + "\n學生已正確放好：" + placed;
    return chat(system, user, 80, 0, 768).trim();
  }

  /**
   * A new sentence with the same grammar pattern, already split into chunks, so it can be
   * played right away. Not cached: each request should give a different sentence.
   */
  @Override
  public SimilarSentenceResponse similar(ScrambleAiRequest request) {
    boolean jp = "jp".equals(request.getLang());
    String language = jp ? "Japanese" : "English";
    String system = "You write practice items for a sentence-scramble game for learners of " + language + ". "
        + "Given a sentence, write ONE new " + language + " sentence that uses the same key grammar pattern, "
        + "with different vocabulary and a different everyday situation, similar length and difficulty. "
        + "Output exactly these lines and nothing else:\n"
        + "SENTENCE: <the new sentence>\n"
        + "CHUNKS: <the same sentence split into 4 to 8 natural chunks, in order, separated by \" | \"; "
        + "joined together they must form the sentence exactly>\n"
        + "ZH: <Traditional Chinese (Taiwan) translation>\n"
        + (jp ? "EN: <English translation>\n" : "")
        + "No furigana, no brackets, no romaji, no numbering. " + PromptRules.TRADITIONAL_EN;
    String user = request.getSentence() + "\n(" + nullToEmpty(request.getTranslation()) + ")";

    // Try twice: an occasional malformed answer shouldn't cost the learner a quota tap.
    for (int attempt = 0; attempt < 2; attempt++) {
      Map<String, String> f = parseFields(chat(system, user, 200, 0.8, 1024));
      String sentence = f.getOrDefault("SENTENCE", "").trim();
      List<String> chunks = Arrays.stream(f.getOrDefault("CHUNKS", "").split("\\|"))
          .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
      String zh = f.getOrDefault("ZH", "").trim();
      if (sentence.isEmpty() || zh.isEmpty()) continue;
      if (!jp) {
        // English chunks are rebuilt on the client from the words; only the sentence matters.
        return new SimilarSentenceResponse(sentence, chunks, zh, "");
      }
      if (chunks.size() >= 3 && compact(String.join("", chunks)).equals(compact(sentence))) {
        return new SimilarSentenceResponse(sentence, chunks, zh, f.getOrDefault("EN", "").trim());
      }
      log.warn("scramble similar: chunks don't rebuild the sentence (attempt {}): {} / {}", attempt + 1, sentence, chunks);
    }
    throw new RuntimeException("scramble similar: no valid sentence");
  }

  /** Up to 3 grammar points behind the sentences the learner struggled with today. */
  @Override
  public String recap(ScrambleRecapRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    String system = "你是一位台灣" + langLabel + "老師。以下是學生今天在句子重組遊戲裡卡住（用了提示、看了答案或重來）的句子。"
        + "請找出這些句子背後他最需要加強的文法或語序重點，用繁體中文整理最多3點，"
        + "每點一行，格式：「- **文法重點**：一句說明。例：一個新的短例句（附中文）」。不要逐句解析。"
        + PromptRules.furiganaZh(request.getLang()) + PromptRules.TRADITIONAL_ZH;
    StringBuilder user = new StringBuilder();
    List<ScrambleRecapRequest.Item> items = request.getItems();
    for (int i = 0; i < items.size(); i++) {
      ScrambleRecapRequest.Item it = items.get(i);
      user.append(i + 1).append(". ").append(it.getSentence())
          .append("（").append(nullToEmpty(it.getTranslation())).append("）\n");
    }
    return chat(system, user.toString(), 380, 0, 1536);
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  private String chat(String system, String userContent, int maxTokens, double temperature, int ctx) {
    Map<String, Object> options = new LinkedHashMap<>();
    options.put("num_predict", maxTokens);
    options.put("num_ctx", ctx);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", llmProperties.getModel());
    body.put("messages", List.of(
        Map.of("role", "system", "content", system),
        Map.of("role", "user", "content", userContent)));
    body.put("think", false);
    body.put("temperature", temperature);
    body.put("stream", false);
    body.put("options", options);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    try {
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.postForObject(llmProperties.getUrl(), entity, Map.class);
      if (response == null) return "查無內容";

      // Try message.content path first (Ollama chat format)
      Object message = response.get("message");
      if (message instanceof Map) {
        @SuppressWarnings("unchecked")
        Object content = ((Map<String, Object>) message).get("content");
        if (content instanceof String) return PromptRules.normalizeFurigana((String) content);
      }
      // Fallback to response field (older Ollama generate format)
      Object resp = response.get("response");
      if (resp instanceof String) return PromptRules.normalizeFurigana((String) resp);

      return "查無內容";
    } catch (Exception e) {
      log.error("ScrambleAI call failed: {}", e.getMessage());
      throw new RuntimeException("AI explain request failed: " + e.getMessage(), e);
    }
  }

  /** Parses "KEY: value" lines into a map (first occurrence wins). */
  private static Map<String, String> parseFields(String raw) {
    Map<String, String> fields = new HashMap<>();
    for (String line : raw.replace('：', ':').split("\n")) {
      int colon = line.indexOf(':');
      if (colon <= 0) continue;
      fields.putIfAbsent(line.substring(0, colon).trim().toUpperCase(), line.substring(colon + 1).trim());
    }
    return fields;
  }

  /** Whitespace-insensitive comparison key (Japanese has no spaces between chunks). */
  private static String compact(String s) {
    return s.replaceAll("[\\s　]", "");
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private String resolveLangLabel(String lang) {
    if ("jp".equals(lang)) return "日文";
    if ("kr".equals(lang)) return "韓文";
    return "英文";
  }
}
