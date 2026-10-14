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
}
