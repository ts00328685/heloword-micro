package com.heloword.frontendapi.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import com.heloword.frontendapi.model.request.ai.ExplainSentenceRequest;
import com.heloword.frontendapi.model.request.ai.LookupRequest;
import com.heloword.frontendapi.model.request.ai.NaturalRequest;
import com.heloword.frontendapi.model.response.ExplainSentenceResponse;
import com.heloword.frontendapi.model.response.LookupResponse;
import com.heloword.frontendapi.model.response.NaturalResponse;
import com.heloword.frontendapi.service.ai.impl.AiFeatureServiceImpl;

/**
 * The lookup controller reads the cache with {@link AiFeatureServiceImpl#lookupKey} before
 * charging quota. If that key ever drifted from the one {@code @Cacheable} stores under,
 * every lookup would silently cost quota again — this pins the two together.
 */
class LookupCacheKeyTest {

  private static LookupRequest request(String word, String lang, String sentence) {
    LookupRequest r = new LookupRequest();
    r.setWord(word);
    r.setWordLang(lang);
    r.setSentence(sentence);
    return r;
  }

  private static Object cacheableKey(LookupRequest r) throws Exception {
    Method m = AiFeatureServiceImpl.class.getMethod("lookup", LookupRequest.class);
    String expression = m.getAnnotation(Cacheable.class).key();
    StandardEvaluationContext ctx = new StandardEvaluationContext();
    ctx.setVariable("request", r);
    return new SpelExpressionParser().parseExpression(expression).getValue(ctx);
  }

  @Test
  void valueStoredByCacheableIsFoundWithTheControllerKey() throws Exception {
    LookupRequest r = request("行いました", "ja", "東京で記者会見を行いました。");
    Cache cache = new ConcurrentMapCache("aiFeature");
    LookupResponse answer = new LookupResponse("行[おこな]う", "舉行", "to hold");

    cache.put(cacheableKey(r), answer);

    LookupRequest sameInputs = request("行いました", "ja", "東京で記者会見を行いました。");
    assertEquals(answer, cache.get(AiFeatureServiceImpl.lookupKey(sameInputs), LookupResponse.class));
  }

  @Test
  void sameWordInADifferentSentenceIsADifferentEntry() throws Exception {
    Cache cache = new ConcurrentMapCache("aiFeature");
    cache.put(cacheableKey(request("bank", "en", "I sat on the river bank.")), new LookupResponse("bank", "河岸", "river edge"));

    assertNull(cache.get(AiFeatureServiceImpl.lookupKey(request("bank", "en", "I went to the bank.")), LookupResponse.class));
    assertNotEquals(
        AiFeatureServiceImpl.lookupKey(request("bank", "en", "a")),
        AiFeatureServiceImpl.lookupKey(request("bank", "ja", "a")));
  }

  @Test
  void naturalAnswerStoredByCacheableIsFoundWithTheControllerKey() throws Exception {
    NaturalRequest r = new NaturalRequest();
    r.setPhrase("How are you?");
    r.setLang("en");
    Method m = AiFeatureServiceImpl.class.getMethod("natural", NaturalRequest.class);
    StandardEvaluationContext ctx = new StandardEvaluationContext();
    ctx.setVariable("request", r);
    Object storedUnder = new SpelExpressionParser().parseExpression(m.getAnnotation(Cacheable.class).key()).getValue(ctx);

    Cache cache = new ConcurrentMapCache("aiFeature");
    NaturalResponse answer = new NaturalResponse("en", "How's it going?", "What's up?", "note");
    cache.put(storedUnder, answer);

    NaturalRequest same = new NaturalRequest();
    same.setPhrase("How are you?");
    same.setLang("en");
    assertEquals(answer, cache.get(AiFeatureServiceImpl.naturalKey(same), NaturalResponse.class));
    // a lookup key for the same text must never collide with it
    assertNotEquals(AiFeatureServiceImpl.naturalKey(same), AiFeatureServiceImpl.lookupKey(request("How are you?", "en", "How are you?")));
  }

  @Test
  void explainSentenceAnswerStoredByCacheableIsFoundWithTheControllerKey() throws Exception {
    ExplainSentenceRequest r = new ExplainSentenceRequest();
    r.setSentence("紅葉は例年より遅く見頃を迎えました。");
    r.setWordLang("jp");
    Method m = AiFeatureServiceImpl.class.getMethod("explainSentence", ExplainSentenceRequest.class);
    StandardEvaluationContext ctx = new StandardEvaluationContext();
    ctx.setVariable("request", r);
    Object storedUnder = new SpelExpressionParser().parseExpression(m.getAnnotation(Cacheable.class).key()).getValue(ctx);

    Cache cache = new ConcurrentMapCache("aiFeature");
    ExplainSentenceResponse answer = new ExplainSentenceResponse(
        List.of(new ExplainSentenceResponse.Part("紅葉[こうよう]は", "主題：紅葉")), List.of(), "紅葉比往年晚進入最佳觀賞期。");
    cache.put(storedUnder, answer);

    ExplainSentenceRequest same = new ExplainSentenceRequest();
    same.setSentence("紅葉は例年より遅く見頃を迎えました。");
    same.setWordLang("jp");
    assertEquals(answer, cache.get(AiFeatureServiceImpl.explainSentenceKey(same), ExplainSentenceResponse.class));
    assertNotEquals(AiFeatureServiceImpl.explainSentenceKey(same),
        AiFeatureServiceImpl.lookupKey(request("紅葉", "jp", "紅葉は例年より遅く見頃を迎えました。")));
  }
}
