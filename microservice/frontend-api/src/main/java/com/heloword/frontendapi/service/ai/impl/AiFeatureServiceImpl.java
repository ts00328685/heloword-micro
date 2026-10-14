package com.heloword.frontendapi.service.ai.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.heloword.frontendapi.model.request.ai.AiWordRequest;
import com.heloword.frontendapi.model.request.ai.CheckSentenceRequest;
import com.heloword.frontendapi.model.request.ai.ExplainWrongRequest;
import com.heloword.frontendapi.model.request.ai.PolitenessRequest;
import com.heloword.frontendapi.model.request.ai.QuickTranslateRequest;
import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.model.request.ai.WordCompareRequest;
import com.heloword.frontendapi.model.request.ai.WordFillRequest;
import com.heloword.frontendapi.model.request.ai.VerbConjugationRequest;
import com.heloword.frontendapi.model.response.CheckSentenceResponse;
import com.heloword.frontendapi.model.response.ClozeResponse;
import com.heloword.frontendapi.model.response.CollocationDto;
import com.heloword.frontendapi.model.response.PolitenessResponse;
import com.heloword.frontendapi.model.response.QuickTranslateResponse;
import com.heloword.frontendapi.model.response.WordFillResponse;
import com.heloword.frontendapi.model.response.VerbConjugationResponse;
import com.heloword.frontendapi.service.ai.AiFeatureService;
import com.heloword.frontendapi.service.ai.PromptRules;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;

@Log4j2
@Service
public class AiFeatureServiceImpl implements AiFeatureService {

  // Cache keys must contain EVERY value that reaches the prompt. The cache is shared by all users,
  // so a key that omits a prompt input (e.g. translateCh) would let one caller inject text into
  // that input and have the result served to everyone who looks up the same word.

  @Autowired
  private LLMProperties llmProperties;

  /** Bounded so a hung LLM can't pin request threads (and guest AI slots) forever. */
  private final RestTemplate restTemplate = new RestTemplateBuilder()
      .setConnectTimeout(Duration.ofSeconds(5))
      .setReadTimeout(Duration.ofSeconds(60))
      .build();

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'insight', #request.wordLang, #request.word, #request.translateEn, #request.translateCh}")
  public String wordInsight(WordInsightRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";
    String ch = request.getTranslateCh() != null ? request.getTranslateCh() : "";

    String system = "你是一位" + langLabel + "老師。請嚴格按照以下格式回覆，不要增減任何欄位，<>內為填寫說明，輸出時不要保留<>：\n"
        + "解釋：<用繁體中文簡短解釋單字的意思，1-2句>\n"
        + langLabel + "例句：<用" + langLabel + "造一個自然的例句>\n"
        + "中文翻譯：<將上面例句翻譯成繁體中文>\n"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    String user = "單字：" + word
        + (en.isEmpty() ? "" : "（英文意思：" + en + "）")
        + (ch.isEmpty() ? "" : "（中文意思：" + ch + "）");

    return callLlm(system, user, 200);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'sample', #request.wordLang, #request.word, #request.translateEn}")
  public String sampleSentence(SampleSentenceRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";

    String system = "你是一位" + langLabel + "老師。請嚴格按照以下格式回覆，不要增減任何欄位，<>內為填寫說明，輸出時不要保留<>：\n"
        + langLabel + "例句：<用" + langLabel + "造一個自然的例句>\n"
        + "中文翻譯：<將上面例句翻譯成繁體中文>\n"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    String user = "單字：" + word + (en.isEmpty() ? "" : "（意思：" + en + "）");

    return callLlm(system, user, 120);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'coach', #request.lang, #request.accuracyPct, #request.wrongCount}")
  public String studyCoach(StudyCoachRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    int pct = request.getAccuracyPct();
    int wrong = request.getWrongCount();

    String system = "你是一位鼓勵學生的" + langLabel + "學習教練。請用繁體中文回覆，給出一句鼓勵的話和一個具體的學習建議，共2句，不要其他內容。"
        + PromptRules.furiganaZh(request.getLang()) + PromptRules.TRADITIONAL_ZH;
    String user = "我最近的背單字正確率是" + pct + "%，答錯了" + wrong + "個單字。";

    return callLlm(system, user, 100);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'compare', #request.wordLang, #request.word, #request.translateEn, #request.translateCh}")
  public String wordCompare(WordCompareRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";
    String ch = request.getTranslateCh() != null ? request.getTranslateCh() : "";

    String system = "你是一位" + langLabel + "老師。請用繁體中文，在3句以內說明該單字有哪些近義詞或相似說法，"
        + "以及各自適合使用的情境，回覆要簡潔。"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    String user = "單字：" + word
        + (en.isEmpty() ? "" : "（英文意思：" + en + "）")
        + (ch.isEmpty() ? "" : "（中文意思：" + ch + "）");

    return callLlm(system, user, 180);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "{'qtrans', #request.text}")
  public QuickTranslateResponse quickTranslate(QuickTranslateRequest request) {
    String text = request.getText() != null ? request.getText().trim() : "";

    String system = "Language detector and translator. Output exactly 4 lines, no extra text:\n"
        + "LANG: <en|de|jp|ch>\n"
        + "WORD: <if Japanese, annotate each kanji immediately with its hiragana reading in square brackets, e.g. 組[く]み合[あ]わせる; otherwise output original text unchanged>\n"
        + "EN: <English meaning, max 6 words>\n"
        + "ZH: <繁體中文意思，最多6字>\n"
        + PromptRules.FURIGANA_EN + " " + PromptRules.TRADITIONAL_EN;
    String raw = callLlm(system, text, 60);
    return parseQuickTranslate(raw, text);
  }

  private QuickTranslateResponse parseQuickTranslate(String raw, String fallbackText) {
    String lang = "en";
    String word = fallbackText;
    String en = "";
    String zh = "";

    for (String line : raw.split("\n")) {
      String t = line.trim();
      if (t.startsWith("LANG:")) lang = t.substring(5).trim().toLowerCase();
      else if (t.startsWith("WORD:")) word = t.substring(5).trim();
      else if (t.startsWith("EN:")) en = t.substring(3).trim();
      else if (t.startsWith("ZH:")) zh = t.substring(3).trim();
    }

    // Normalise lang code to match frontend TTS_LANG_MAP keys
    if (lang.equals("ja")) lang = "jp";
    if (lang.equals("zh")) lang = "ch";

    return new QuickTranslateResponse(word, lang, en, zh);
  }

  /**
   * @param maxTokens hard cap on generated tokens — the main lever on latency for a small local model
   */
  private String callLlm(String system, String userContent, int maxTokens) {
    Map<String, Object> body = Map.of(
        "model", llmProperties.getModel(),
        "messages", List.of(
            Map.of("role", "system", "content", system),
            Map.of("role", "user", "content", userContent)
        ),
        "think", false,
        "temperature", 0,
        "stream", false,
        "keep_alive", "30m",
        "options", Map.of("num_predict", maxTokens)
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
        if (content instanceof String) return PromptRules.normalizeFurigana((String) content);
      }
      Object resp = response.get("response");
      if (resp instanceof String) return PromptRules.normalizeFurigana((String) resp);

      return "查無內容";
    } catch (Exception e) {
      log.error("AI feature call failed: {}", e.getMessage());
      throw new RuntimeException("AI request failed: " + e.getMessage(), e);
    }
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'fill', #request.wordLang, #request.word}")
  public WordFillResponse wordFill(WordFillRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();

    String system = "你是一位語言老師。根據給定的" + langLabel + "單字，嚴格只輸出以下5行，不要任何額外文字或標點：\n"
        + "MEANING_EN: <簡短英文意思，不超過8個英文單字>\n"
        + "MEANING_ZH: <簡短繁體中文意思，不超過8個字>\n"
        + "SENTENCE: <用" + langLabel + "造一個自然的例句，不可使用其他語言>\n"
        + "SENTENCE_ZH: <將上面例句翻譯成繁體中文>\n"
        + "LEVEL: <日文用JLPT N5-N1，其他語言用CEFR A1-C2>\n"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    String user = word;

    String raw = callLlm(system, user, 160);
    return parseWordFill(raw);
  }

  private WordFillResponse parseWordFill(String raw) {
    String translateEn = "";
    String translateCh = "";
    String sentence = "";
    String sentenceZh = "";
    String level = "";

    for (String line : raw.split("\n")) {
      String trimmed = line.trim();
      if (trimmed.startsWith("MEANING_EN:")) {
        translateEn = trimmed.substring("MEANING_EN:".length()).trim();
      } else if (trimmed.startsWith("MEANING_ZH:")) {
        translateCh = trimmed.substring("MEANING_ZH:".length()).trim();
      } else if (trimmed.startsWith("SENTENCE_ZH:")) {
        sentenceZh = trimmed.substring("SENTENCE_ZH:".length()).trim();
      } else if (trimmed.startsWith("LEVEL:")) {
        level = normalizeLevel(trimmed.substring("LEVEL:".length()));
      } else if (trimmed.startsWith("SENTENCE:")) {
        sentence = trimmed.substring("SENTENCE:".length()).trim();
      }
    }

    String combined = sentence;
    if (!sentenceZh.isEmpty()) {
      combined = sentence + " " + sentenceZh;
    }
    return new WordFillResponse(translateEn, translateCh, combined, level);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "{'verbconj', #request.word}")
  public VerbConjugationResponse verbConjugation(VerbConjugationRequest request) {
    String word = request.getWord() != null ? request.getWord().trim() : "";

    String system = "You are a verb conjugation assistant. Auto-detect if the input is an English verb or Japanese verb.\n"
        + "Output EXACTLY the following lines with NO extra text:\n"
        + "LANG: <en|jp>\n"
        + "WORD: <base form; if Japanese annotate kanji with hiragana in square brackets e.g. 食[た]べる>\n"
        + "MEANING_EN: <English meaning, max 6 words>\n"
        + "MEANING_ZH: <繁體中文意思，最多6字>\n"
        + "If LANG is en, output these 3 lines:\n"
        + "BASE: <base form>\n"
        + "PAST: <simple past>\n"
        + "PAST_PART: <past participle>\n"
        + "If LANG is jp, output these 10 lines:\n"
        + "辞書形: <dictionary form>\n"
        + "ます形: <masu form>\n"
        + "ない形: <nai form>\n"
        + "て形: <te form>\n"
        + "た形: <ta form>\n"
        + "可能形: <potential form>\n"
        + "意向形: <volitional form>\n"
        + "命令形: <imperative form>\n"
        + "被動形: <passive form>\n"
        + "使役形: <causative form>\n"
        + "If the input is NOT a verb at all, output exactly: ERROR: not_a_verb\n"
        + PromptRules.FURIGANA_EN + " " + PromptRules.TRADITIONAL_EN;
    String raw = callLlm(system, word, 220);
    return parseVerbConjugation(raw, word);
  }

  private VerbConjugationResponse parseVerbConjugation(String raw, String fallbackWord) {
    if (raw.contains("ERROR: not_a_verb")) {
      throw HeloServiceException.of(ResponseCode.NOT_A_VERB);
    }

    String lang = "en";
    String parsedWord = fallbackWord;
    String meaningEn = "";
    String meaningZh = "";
    StringBuilder conj = new StringBuilder();

    for (String line : raw.split("\n")) {
      String t = line.trim();
      if (t.startsWith("LANG:")) { lang = t.substring(5).trim().toLowerCase(); }
      else if (t.startsWith("WORD:")) { parsedWord = t.substring(5).trim(); }
      else if (t.startsWith("MEANING_EN:")) { meaningEn = t.substring(11).trim(); }
      else if (t.startsWith("MEANING_ZH:")) { meaningZh = t.substring(11).trim(); }
      else if (t.startsWith("BASE:") || t.startsWith("PAST_PART:") || t.startsWith("PAST:")) {
        conj.append(t).append("\n");
      } else if (t.startsWith("辞書形:") || t.startsWith("ます形:") || t.startsWith("ない形:")
          || t.startsWith("て形:") || t.startsWith("た形:") || t.startsWith("可能形:")
          || t.startsWith("意向形:") || t.startsWith("命令形:") || t.startsWith("被動形:")
          || t.startsWith("使役形:")) {
        conj.append(t).append("\n");
      }
    }

    if (lang.equals("ja")) lang = "jp";
    return new VerbConjugationResponse(parsedWord, lang, meaningEn, meaningZh, conj.toString().trim());
  }

  // ── Learning tools ─────────────────────────────────────────────────────────
  // System prompts stay fixed per language so Ollama can reuse its prompt cache;
  // everything user-specific goes in the user message.

  @Override
  public CheckSentenceResponse checkSentence(CheckSentenceRequest request) {
    String wordLang = request.getWordLang();
    String system = "你是" + resolveWordLangLabel(wordLang) + "老師。判斷學生用指定單字造的句子文法和用法是否正確。只輸出2行：\n"
        + "OK: <yes|no>\n"
        + "FIX: <若no，寫出修正後的句子，再用一句繁體中文說明原因；若yes，留空>\n"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    String user = "單字：" + request.getWord() + "\n句子：" + request.getSentence();

    Map<String, String> f = parseFields(callLlm(system, user, 120));
    boolean ok = f.getOrDefault("OK", "").toLowerCase().startsWith("y");
    return new CheckSentenceResponse(ok, ok ? "" : f.getOrDefault("FIX", ""));
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "{'hook', #request.wordLang, #request.word, #request.meaningZh}")
  public String memoryHook(AiWordRequest request) {
    String wordLang = request.getWordLang();
    String system = "用一句繁體中文給出單字的記憶法（諧音、拆字或聯想皆可），不超過30字，只輸出那一句。"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    String meaning = request.getMeaningZh();
    String user = request.getWord() + (meaning.isEmpty() ? "" : "（" + meaning + "）");
    return callLlm(system, user, 60).trim();
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "{'wrong', #request.wordLang, #request.word, #request.picked, #request.answer}")
  public String explainWrong(ExplainWrongRequest request) {
    String wordLang = request.getWordLang();
    String system = "學生在單字測驗選錯答案。用最多2句繁體中文說明選的答案為何不對，並點出正確答案的關鍵差異。"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    String user = "題目：" + request.getWord() + "\n學生選：" + request.getPicked() + "\n正確答案：" + request.getAnswer();
    return callLlm(system, user, 100).trim();
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "{'cloze', #request.wordLang, #request.word}")
  public ClozeResponse cloze(AiWordRequest request) {
    String wordLang = request.getWordLang();
    String langLabel = resolveWordLangLabel(wordLang);
    String system = "用" + langLabel + "造一個含指定單字的自然短句，把該單字（含變化形）換成____。只輸出3行：\n"
        + "CLOZE: <挖空後的句子>\n"
        + "ANSWER: <被挖掉的實際字形>\n"
        + "ZH: <整句的繁體中文翻譯>\n"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;

    Map<String, String> f = parseFields(callLlm(system, request.getWord(), 120));
    String sentence = f.getOrDefault("CLOZE", "");
    if (!sentence.contains("____")) throw new RuntimeException("cloze output missing blank");
    return new ClozeResponse(sentence, f.getOrDefault("ANSWER", request.getWord()), f.getOrDefault("ZH", ""));
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "{'polite', #request.sentence}")
  public PolitenessResponse politeness(PolitenessRequest request) {
    String system = "把日文句子改寫成3種語氣，意思不變。只輸出3行：\n"
        + "CASUAL: <普通形・タメ口>\n"
        + "POLITE: <です・ます形>\n"
        + "KEIGO: <敬語（尊敬語或謙讓語）>\n"
        + PromptRules.FURIGANA_ZH;

    Map<String, String> f = parseFields(callLlm(system, request.getSentence(), 150));
    return new PolitenessResponse(f.getOrDefault("CASUAL", ""), f.getOrDefault("POLITE", ""), f.getOrDefault("KEIGO", ""));
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "{'colloc', #request.wordLang, #request.word}")
  public List<CollocationDto> collocations(AiWordRequest request) {
    String wordLang = request.getWordLang();
    String system = "列出該" + resolveWordLangLabel(wordLang) + "單字最常見的3個搭配用法。只輸出3行，每行格式：搭配 | 繁體中文意思\n"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;

    List<CollocationDto> result = new ArrayList<>();
    for (String line : callLlm(system, request.getWord(), 100).split("\n")) {
      int bar = line.indexOf('|');
      if (bar <= 0) continue;
      String phrase = line.substring(0, bar).replaceFirst("^[\\s\\d.、\\-*]+", "").trim();
      String meaning = line.substring(bar + 1).trim();
      if (!phrase.isEmpty()) result.add(new CollocationDto(phrase, meaning));
      if (result.size() == 3) break;
    }
    return result;
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "{'level', #request.wordLang, #request.word}")
  public String wordLevel(AiWordRequest request) {
    String system = "Output only the difficulty level of the word: JLPT N5-N1 for Japanese, CEFR A1-C2 for any other language. No other text.";
    return normalizeLevel(callLlm(system, request.getWord(), 8));
  }

  @Override
  public String[] weeklyStory(List<String> words, String wordLang) {
    String langLabel = resolveWordLangLabel(wordLang);
    String system = "用" + langLabel + "寫一篇約60字、有趣好懂的小故事，必須用到使用者給的每一個單字。只輸出2行：\n"
        + "STORY: <故事>\n"
        + "ZH: <故事的繁體中文翻譯>\n"
        + PromptRules.furiganaZh(wordLang) + PromptRules.TRADITIONAL_ZH;
    Map<String, String> f = parseFields(callLlm(system, String.join(", ", words), 400));
    String story = f.getOrDefault("STORY", "");
    if (story.isEmpty()) throw new RuntimeException("weekly story output missing STORY");
    return new String[] { story, f.getOrDefault("ZH", "") };
  }

  /** Accepts only N1–N5 / A1–C2; anything else becomes "" so junk never reaches the DB. */
  private String normalizeLevel(String raw) {
    Matcher m = LEVEL.matcher(raw == null ? "" : raw.toUpperCase());
    return m.find() ? m.group() : "";
  }

  private static final Pattern LEVEL = Pattern.compile("\\b(N[1-5]|[ABC][12])\\b");

  /** Parses "KEY: value" lines into a map (first occurrence wins). */
  private Map<String, String> parseFields(String raw) {
    Map<String, String> fields = new HashMap<>();
    for (String line : raw.replace('：', ':').split("\n")) {
      int colon = line.indexOf(':');
      if (colon <= 0) continue;
      fields.putIfAbsent(line.substring(0, colon).trim().toUpperCase(), line.substring(colon + 1).trim());
    }
    return fields;
  }

  /** Resolves UI language code (i18n) to a display label. */
  private String resolveLangLabel(String lang) {
    if ("ja".equals(lang) || "jp".equals(lang)) return "日文";
    if ("ko".equals(lang) || "kr".equals(lang)) return "韓文";
    return "英文";
  }

  /** Resolves the word's own language code to a display label used in prompts. */
  private String resolveWordLangLabel(String wordLang) {
    if (wordLang == null) return "英文";
    if ("ja".equals(wordLang) || "jp".equals(wordLang)) return "日文";
    if ("de".equals(wordLang)) return "德文";
    if ("ko".equals(wordLang) || "kr".equals(wordLang)) return "韓文";
    if ("zh".equals(wordLang) || "ch".equals(wordLang)) return "中文";
    return "英文";
  }
}
