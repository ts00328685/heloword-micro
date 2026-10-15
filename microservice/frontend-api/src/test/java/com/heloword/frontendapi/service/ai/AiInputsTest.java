package com.heloword.frontendapi.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AiInputsTest {

  @Test
  void knownCodesArePassedThroughLowercased() {
    assertEquals("jp", AiInputs.wordLang("jp", "apple"));
    assertEquals("ja", AiInputs.wordLang("JA", "apple"));
    assertEquals("de", AiInputs.wordLang("de", "Apfel"));
  }

  @Test
  void customWordsAreDetectedFromTheText() {
    assertEquals("jp", AiInputs.wordLang("custom", "食べる"));
    assertEquals("jp", AiInputs.wordLang("custom", "カメラ"));
    assertEquals("jp", AiInputs.wordLang("custom", "日本"));
    assertEquals("ko", AiInputs.wordLang("custom", "사과"));
    assertEquals("en", AiInputs.wordLang("custom", "apple"));
    assertEquals("en", AiInputs.wordLang(null, "apple"));
  }

  @Test
  void naturalAcceptsEnglishOrJapaneseOnly() {
    assertEquals("en", AiInputs.englishOrJapanese("How are you?"));
    assertEquals("jp", AiInputs.englishOrJapanese("お元気ですか"));
    assertEquals("jp", AiInputs.englishOrJapanese("コーヒー"));
    // mixed: any kana means Japanese
    assertEquals("jp", AiInputs.englishOrJapanese("OKです"));
    // Chinese (kanji, no kana) never reaches the model
    org.junit.jupiter.api.Assertions.assertThrows(com.heloword.common.exception.HeloServiceException.class,
        () -> AiInputs.englishOrJapanese("我想喝咖啡"));
    org.junit.jupiter.api.Assertions.assertThrows(com.heloword.common.exception.HeloServiceException.class,
        () -> AiInputs.englishOrJapanese("12345"));
  }
}
