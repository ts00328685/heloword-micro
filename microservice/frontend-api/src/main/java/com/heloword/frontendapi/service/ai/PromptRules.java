package com.heloword.frontendapi.service.ai;

import java.util.regex.Pattern;

/**
 * Shared output rules appended to every LLM prompt so all features agree on
 * furigana format (漢字[かな]) and Traditional Chinese output.
 */
public final class PromptRules {

  /** Furigana rule for Chinese-language prompts. */
  public static final String FURIGANA_ZH =
      "若內容含有日文，請在每個漢字（或連續漢字詞）後面緊接著用半形方括號[]標注平假名讀音，"
      + "例如：その曲[きょく]、食[た]べる、日本語[にほんご]；絕對不可使用圓括號（）或()標注讀音。"
      + "只標注日文漢字，英文或其他非日文單字絕對不可加注假名。";

  /** For non-Japanese content: stops the model from inventing readings for English words. */
  public static final String NO_FURIGANA_ZH =
      "此內容不是日文，請勿加注任何平假名、片假名或讀音標記。";

  /** Traditional Chinese rule for Chinese-language prompts. */
  public static final String TRADITIONAL_ZH =
      "所有中文一律使用台灣繁體中文（正體字），絕對不可出現任何簡體字。";

  /** Furigana rule for English-language prompts. */
  public static final String FURIGANA_EN =
      "If any Japanese appears, annotate each kanji (or kanji compound) immediately with its hiragana reading "
      + "in half-width square brackets, e.g. その曲[きょく], 食[た]べる, 日本語[にほんご]. "
      + "Never use parentheses （） or () for readings. "
      + "Only annotate Japanese kanji; never add readings to English or other non-Japanese words.";

  /** Traditional Chinese rule for English-language prompts. */
  public static final String TRADITIONAL_EN =
      "Any Chinese text must be Traditional Chinese as used in Taiwan (繁體中文); never use Simplified Chinese characters.";

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
