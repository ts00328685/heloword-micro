package com.heloword.frontendapi.service.ai.impl;

import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.heloword.frontendapi.config.CacheConfig;
import com.heloword.frontendapi.model.request.ai.QuickTranslateRequest;
import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.model.request.ai.WordCompareRequest;
import com.heloword.frontendapi.model.request.ai.WordFillRequest;
import com.heloword.frontendapi.model.request.ai.VerbConjugationRequest;
import com.heloword.frontendapi.model.response.QuickTranslateResponse;
import com.heloword.frontendapi.model.response.WordFillResponse;
import com.heloword.frontendapi.model.response.VerbConjugationResponse;
import com.heloword.frontendapi.service.ai.AiFeatureService;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;

@Log4j2
@Service
public class AiFeatureServiceImpl implements AiFeatureService {

  private static final String LLM_URL = "https://tunnel.heloword.com/api/chat";
  private static final String MODEL = "gemma4:26b-a4b-it-q4_K_M";

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "'insight:' + (#request.wordLang ?: 'en') + ':' + #request.word.toLowerCase()")
  public String wordInsight(WordInsightRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";
    String ch = request.getTranslateCh() != null ? request.getTranslateCh() : "";

    String system = "你是一位" + langLabel + "老師。請嚴格按照以下格式回覆，不要增減任何欄位，若為日文，請在日文例句的漢字旁用括號標注假名：\n"
        + "解釋：（用繁體中文簡短解釋單字的意思，1-2句）\n"
        + langLabel + "例句：（用" + langLabel + "造一個自然的例句）\n"
        + "中文翻譯：（將上面例句翻譯成繁體中文）";
    String user = "單字：" + word
        + (en.isEmpty() ? "" : "（英文意思：" + en + "）")
        + (ch.isEmpty() ? "" : "（中文意思：" + ch + "）");

    return callLlm(system, user);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "'sample:' + (#request.wordLang ?: 'en') + ':' + #request.word.toLowerCase()")
  public String sampleSentence(SampleSentenceRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";

    String system = "你是一位" + langLabel + "老師。請嚴格按照以下格式回覆，不要增減任何欄位，若為日文，請在日文例句的漢字旁用括號標注假名：\n"
        + langLabel + "例句：（用" + langLabel + "造一個自然的例句）\n"
        + "中文翻譯：（將上面例句翻譯成繁體中文）";
    String user = "單字：" + word + (en.isEmpty() ? "" : "（意思：" + en + "）");

    return callLlm(system, user);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "'coach:' + (#request.lang ?: 'zh') + ':' + #request.accuracyPct + ':' + #request.wrongCount")
  public String studyCoach(StudyCoachRequest request) {
    String langLabel = resolveLangLabel(request.getLang());
    int pct = request.getAccuracyPct();
    int wrong = request.getWrongCount();

    String system = "你是一位鼓勵學生的" + langLabel + "學習教練。請用繁體中文回覆，給出一句鼓勵的話和一個具體的學習建議，共2句，不要其他內容。";
    String user = "我最近的背單字正確率是" + pct + "%，答錯了" + wrong + "個單字。";

    return callLlm(system, user);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "'compare:' + (#request.wordLang ?: 'en') + ':' + #request.word.toLowerCase()")
  public String wordCompare(WordCompareRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();
    String en = request.getTranslateEn() != null ? request.getTranslateEn() : "";
    String ch = request.getTranslateCh() != null ? request.getTranslateCh() : "";

    String system = "你是一位" + langLabel + "老師。請用繁體中文，在3句以內說明「" + word
        + "」有哪些近義詞或相似說法，以及各自適合使用的情境，回覆要簡潔。";
    String user = "單字：" + word
        + (en.isEmpty() ? "" : "（英文意思：" + en + "）")
        + (ch.isEmpty() ? "" : "（中文意思：" + ch + "）");

    return callLlm(system, user);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "'qtrans2:' + #request.text.toLowerCase()")
  public QuickTranslateResponse quickTranslate(QuickTranslateRequest request) {
    String text = request.getText() != null ? request.getText().trim() : "";

    String system = "Language detector and translator. Output exactly 4 lines, no extra text:\n"
        + "LANG: <en|de|jp|ch>\n"
        + "WORD: <if Japanese, annotate each kanji immediately with its hiragana reading in square brackets, e.g. 組[く]み合[あ]わせる; otherwise output original text unchanged>\n"
        + "EN: <English meaning, max 6 words>\n"
        + "ZH: <繁體中文意思，最多6字>";
    String raw = callLlm(system, text);
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

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE,
      key = "'fill:' + (#request.wordLang ?: 'en') + ':' + #request.word.toLowerCase()")
  public WordFillResponse wordFill(WordFillRequest request) {
    String wordLang = request.getWordLang() != null ? request.getWordLang() : "en";
    String langLabel = resolveWordLangLabel(wordLang);
    String word = request.getWord();

    String system = "你是一位語言老師。根據給定的" + langLabel + "單字，嚴格只輸出以下4行，不要任何額外文字或標點：\n"
        + "MEANING_EN: <簡短英文意思，不超過8個英文單字>\n"
        + "MEANING_ZH: <簡短繁體中文意思，不超過8個字>\n"
        + "SENTENCE: <用" + langLabel + "造一個自然的例句，不可使用其他語言>\n"
        + "SENTENCE_ZH: <將上面例句翻譯成繁體中文>";
    String user = word;

    String raw = callLlm(system, user);
    return parseWordFill(raw);
  }

  private WordFillResponse parseWordFill(String raw) {
    String translateEn = "";
    String translateCh = "";
    String sentence = "";
    String sentenceZh = "";

    for (String line : raw.split("\n")) {
      String trimmed = line.trim();
      if (trimmed.startsWith("MEANING_EN:")) {
        translateEn = trimmed.substring("MEANING_EN:".length()).trim();
      } else if (trimmed.startsWith("MEANING_ZH:")) {
        translateCh = trimmed.substring("MEANING_ZH:".length()).trim();
      } else if (trimmed.startsWith("SENTENCE_ZH:")) {
        sentenceZh = trimmed.substring("SENTENCE_ZH:".length()).trim();
      } else if (trimmed.startsWith("SENTENCE:")) {
        sentence = trimmed.substring("SENTENCE:".length()).trim();
      }
    }

    String combined = sentence;
    if (!sentenceZh.isEmpty()) {
      combined = sentence + " " + sentenceZh;
    }
    return new WordFillResponse(translateEn, translateCh, combined);
  }

  @Override
  @Cacheable(value = CacheConfig.AI_CACHE, key = "'verbconj:' + #request.word.toLowerCase()")
  public VerbConjugationResponse verbConjugation(VerbConjugationRequest request) {
    String word = request.getWord() != null ? request.getWord().trim() : "";

    String system = "You are a verb conjugation assistant. Auto-detect if the input is an English verb or Japanese verb.\n"
        + "Output EXACTLY the following lines with NO extra text:\n"
        + "LANG: <en|jp>\n"
        + "WORD: <base form; if Japanese annotate kanji with hiragana in brackets e.g. 食[た]べる>\n"
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
        + "If the input is NOT a verb at all, output exactly: ERROR: not_a_verb";
    String raw = callLlm(system, word);
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
