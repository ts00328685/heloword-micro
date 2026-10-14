package com.heloword.frontendapi.service.ai;

import java.util.regex.Pattern;

/**
 * Shared output rules appended to every LLM prompt so all features agree on
 * furigana format (漢字[かな]) and Traditional Chinese output.
 */
public final class PromptRules {

  /** Furigana rule for Chinese-language prompts. */
  public static final String FURIGANA_ZH =
      "日文漢字後緊接半形方括號標平假名，如その曲[きょく]、食[た]べる；禁用()或（）；英文等非日文不可標假名。";

  /** For non-Japanese content: stops the model from inventing readings for English words. */
  public static final String NO_FURIGANA_ZH =
      "此非日文內容，勿標任何假名。";

  /** Traditional Chinese rule for Chinese-language prompts. */
  public static final String TRADITIONAL_ZH =
      "中文一律用台灣繁體字，禁用簡體字。";

  /** Furigana rule for English-language prompts. */
  public static final String FURIGANA_EN =
      "Japanese kanji: put the hiragana reading right after it in square brackets, e.g. 食[た]べる, 日本語[にほんご]; "
      + "never use (); never annotate non-Japanese words.";

  /** Traditional Chinese rule for English-language prompts. */
  public static final String TRADITIONAL_EN =
      "Chinese must be Traditional (Taiwan), never Simplified.";

  /** Kanji run followed by a hiragana-only reading in full- or half-width parentheses. */
  private static final Pattern PAREN_FURIGANA =
      Pattern.compile("([\\p{IsHan}々〆ヶ]+)[（(]([\\p{IsHiragana}ー]+)[）)]");

  private PromptRules() {}

  public static boolean isJapanese(String langCode) {
    return "ja".equalsIgnoreCase(langCode) || "jp".equalsIgnoreCase(langCode);
  }

  /** Furigana rule for Chinese-language prompts, chosen by the content's language code. */
  public static String furiganaZh(String langCode) {
    return isJapanese(langCode) ? FURIGANA_ZH : NO_FURIGANA_ZH;
  }

  /** Safety net: rewrites 曲（きょく） / 曲(きょく) into 曲[きょく] if the model ignores the rule. */
  public static String normalizeFurigana(String text) {
    if (text == null || text.isEmpty()) return text;
    return PAREN_FURIGANA.matcher(text).replaceAll("$1[$2]");
  }
}
