package com.heloword.frontendapi.service.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.heloword.frontendapi.config.LLMProperties;
import com.heloword.frontendapi.model.request.ai.NaturalRequest;
import com.heloword.frontendapi.model.response.NaturalResponse;
import com.heloword.frontendapi.service.ai.impl.AiFeatureServiceImpl;

/**
 * Runs the real "natural vs textbook" prompt against a live Ollama and checks the parsed result.
 * Opt-in: HELO_LLM_IT=1 (optionally HELO_LLM_IT_URL / HELO_LLM_IT_MODEL). Never runs in CI.
 */
@EnabledIfEnvironmentVariable(named = "HELO_LLM_IT", matches = "1")
class NaturalPromptIT {

  private AiFeatureServiceImpl service() throws Exception {
    LLMProperties props = new LLMProperties();
    props.setUrl(System.getenv().getOrDefault("HELO_LLM_IT_URL", "http://localhost:11434/api/chat"));
    props.setModel(System.getenv().getOrDefault("HELO_LLM_IT_MODEL", "gemma4:26b-a4b-it-q4_K_M"));
    AiFeatureServiceImpl s = new AiFeatureServiceImpl();
    Field f = AiFeatureServiceImpl.class.getDeclaredField("llmProperties");
    f.setAccessible(true);
    f.set(s, props);
    return s;
  }

  private NaturalResponse ask(String phrase) throws Exception {
    NaturalRequest r = new NaturalRequest();
    r.setPhrase(phrase);
    r.setLang(AiInputs.englishOrJapanese(phrase));
    NaturalResponse res = service().natural(r);
    System.out.println(phrase + " -> " + res);
    return res;
  }

  private static boolean hasCjk(String s) { return s.codePoints().anyMatch(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN); }
  private static boolean hasKana(String s) { return s.matches("(?s).*[\\u3041-\\u30ff].*"); }

  @Test
  void englishStaysEnglishWithAChineseNote() throws Exception {
    NaturalResponse r = ask("Could you please tell me the way to the station?");
    assertFalse(hasCjk(r.getNatural()) || hasKana(r.getNatural()), "natural should be English");
    assertFalse(hasCjk(r.getCasual()) || hasKana(r.getCasual()), "casual should be English");
    assertTrue(hasCjk(r.getNote()), "note should be Chinese");
    assertFalse(r.getNote().matches("(?s).*(NATURAL|CASUAL|Natural|Casual).*"), "note must not use English labels");
  }

  @Test
  void japaneseStaysJapaneseWithSquareBracketReadings() throws Exception {
    NaturalResponse r = ask("私はコーヒーを飲みたいです");
    assertTrue(hasKana(r.getNatural()) && hasKana(r.getCasual()), "both versions should be Japanese");
    assertFalse(r.getNatural().contains("(") || r.getNatural().contains("（"), "readings must use [] not ()");
    assertTrue(r.getCasual().contains("["), "casual should carry readings");
  }
}
