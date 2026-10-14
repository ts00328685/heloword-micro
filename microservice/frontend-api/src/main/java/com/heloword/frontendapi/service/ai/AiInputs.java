package com.heloword.frontendapi.service.ai;

import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;

/**
 * Input checks for AI endpoints. Long inputs cost GPU time and widen the prompt-injection
 * surface, so every user-supplied field is length-capped before it reaches a prompt.
 */
public final class AiInputs {

  public static final int WORD_MAX = 60;
  public static final int MEANING_MAX = 120;
  public static final int SENTENCE_MAX = 300;

  private static final Set<String> WORD_LANGS = Set.of("en", "jp", "ja", "de", "ko", "kr", "zh", "ch");
  private static final Pattern JAPANESE = Pattern.compile("[\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}]");

  private AiInputs() {}

  /** Required field: trimmed, non-blank, at most {@code max} chars. */
  public static String required(String value, int max) {
    String v = StringUtils.trimToEmpty(value);
    if (v.isEmpty() || v.length() > max) throw HeloServiceException.of(ResponseCode.INVALID_REQUEST);
    return v;
  }

  /** Optional field: trimmed, empty if null, at most {@code max} chars. */
  public static String optional(String value, int max) {
    String v = StringUtils.trimToEmpty(value);
    if (v.length() > max) throw HeloServiceException.of(ResponseCode.INVALID_REQUEST);
    return v;
  }

  /** Language code from a fixed whitelist; anything else falls back to English. */
  public static String wordLang(String value) {
    String v = StringUtils.lowerCase(StringUtils.trimToEmpty(value));
    return WORD_LANGS.contains(v) ? v : "en";
  }

  /**
   * Like {@link #wordLang(String)}, but when the code isn't recognised (custom vocab words arrive
   * as "custom") the language is detected from the word itself, so a Japanese custom word still
   * gets Japanese prompts and furigana instead of being treated as English.
   */
  public static String wordLang(String value, String text) {
    String v = StringUtils.lowerCase(StringUtils.trimToEmpty(value));
    if (WORD_LANGS.contains(v)) return v;
    String t = StringUtils.defaultString(text);
    if (KANA.matcher(t).find()) return "jp";
    if (HANGUL.matcher(t).find()) return "ko";
    if (HAN.matcher(t).find()) return "jp";  // kanji-only words: the app teaches Japanese, not Chinese
    return "en";
  }

  private static final Pattern KANA = Pattern.compile("[\\p{IsHiragana}\\p{IsKatakana}]");
  private static final Pattern HANGUL = Pattern.compile("\\p{IsHangul}");
  private static final Pattern HAN = Pattern.compile("\\p{IsHan}");

  /** Rejects text with no kana/kanji before it ever reaches the model. */
  public static String japanese(String value, int max) {
    String v = required(value, max);
    if (!JAPANESE.matcher(v).find()) throw HeloServiceException.of(ResponseCode.INVALID_REQUEST);
    return v;
  }
}
