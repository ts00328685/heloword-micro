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
      + "例如：その曲[きょく]、食[た]べる、日本語[にほんご]；絕對不可使用圓括號（）或()標注讀音。";

  /** Traditional Chinese rule for Chinese-language prompts. */
  public static final String TRADITIONAL_ZH =
      "所有中文一律使用台灣繁體中文（正體字），絕對不可出現任何簡體字。";

  /** Furigana rule for English-language prompts. */
  public static final String FURIGANA_EN =
      "If any Japanese appears, annotate each kanji (or kanji compound) immediately with its hiragana reading "
      + "in half-width square brackets, e.g. その曲[きょく], 食[た]べる, 日本語[にほんご]. "
      + "Never use parentheses （） or () for readings.";

  /** Traditional Chinese rule for English-language prompts. */
  public static final String TRADITIONAL_EN =
      "Any Chinese text must be Traditional Chinese as used in Taiwan (繁體中文); never use Simplified Chinese characters.";

  /** Kanji run followed by a hiragana-only reading in full- or half-width parentheses. */
  private static final Pattern PAREN_FURIGANA =
      Pattern.compile("([\\p{IsHan}々〆ヶ]+)[（(]([\\p{IsHiragana}ー]+)[）)]");

  private PromptRules() {}

  /** Safety net: rewrites 曲（きょく） / 曲(きょく) into 曲[きょく] if the model ignores the rule. */
  public static String normalizeFurigana(String text) {
    if (text == null || text.isEmpty()) return text;
    return PAREN_FURIGANA.matcher(text).replaceAll("$1[$2]");
  }
}
