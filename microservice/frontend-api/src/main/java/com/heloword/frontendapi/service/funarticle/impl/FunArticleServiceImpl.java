package com.heloword.frontendapi.service.funarticle.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.heloword.common.entity.funarticle.FunArticleEntity;
import com.heloword.common.entity.word.WordEnglishEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.feignclient.ServiceWordClient;
import com.heloword.frontendapi.config.CacheConfig;
import com.heloword.frontendapi.model.response.FunArticleDto;
import com.heloword.frontendapi.service.funarticle.FunArticleService;

@Log4j2
@Service
public class FunArticleServiceImpl implements FunArticleService {

  private static final String LLM_URL = "https://tunnel.heloword.com/api/chat";
  private static final String MODEL = "gemma4:26b-a4b-it-q4_K_M";
  private static final int ARTICLE_COUNT = 200;

  @Autowired
  private ServiceWordClient serviceWordClient;

  @Autowired
  private ServiceRecordClient serviceRecordClient;

  /** Self-injection through proxy so getLatest() benefits from getAll() cache. */
  @Autowired
  @Lazy
  private FunArticleService self;

  private final RestTemplate restTemplate = new RestTemplate(
      new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
        setConnectTimeout(10_000);
        setReadTimeout(10 * 60 * 1000);
      }});

  /** Runs daily at 03:00 Taipei time. Generates articles and persists them via service-record. */
//  @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Taipei")
  @CacheEvict(value = CacheConfig.FUN_ARTICLE_CACHE, allEntries = true)
  public void refresh() {
    try {
      List<WordEnglishEntity> allWords = serviceWordClient.getAllEnWords().getData();
      if (allWords == null || allWords.isEmpty()) {
        log.warn("FunArticle: no English words available");
        return;
      }
      Collections.shuffle(allWords);
      List<WordEnglishEntity> picked = allWords.subList(0, Math.min(ARTICLE_COUNT, allWords.size()));

      for (WordEnglishEntity word : picked) {
        try {
          String content = generateArticle(word.getWord());
          FunArticleEntity entity = new FunArticleEntity();
          entity.setWord(word.getWord());
          entity.setContent(content);
          serviceRecordClient.saveFunArticle(entity);
          log.info("FunArticle: saved article for '{}'", word.getWord());
        } catch (Exception e) {
          log.error("FunArticle: failed to generate for word '{}': {}", word.getWord(), e.getMessage());
        }
      }
      log.info("FunArticle: refresh complete");
    } catch (Exception e) {
      log.error("FunArticle: refresh failed: {}", e.getMessage(), e);
    }
  }

  @Override
  public FunArticleDto getLatest() {
    List<FunArticleDto> articles = self.getAll();
    return articles.isEmpty() ? null : articles.get(0);
  }

  @Override
  @Cacheable(value = CacheConfig.FUN_ARTICLE_CACHE, key = "'all'", unless = "#result.isEmpty()")
  public List<FunArticleDto> getAll() {
    List<FunArticleEntity> entities = serviceRecordClient.getRandomFunArticles().getData();
    if (entities == null) return Collections.emptyList();
    return entities.stream()
        .map(e -> new FunArticleDto(e.getWord(), e.getContent()))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private String generateArticle(String word) {
    String system = "You are a language teacher who writes short, fun articles to help people memorize English words. "
        + "You always follow the exact output format given to you. Do not add any extra commentary or sections.";
    String user = "Write a fun article for the English word \"" + word + "\" using this exact format:\n\n"
        + "## " + word + "\n\n"
        + "[Write a funny, easy-to-understand article of about 80-100 words using the word \"" + word + "\" at least twice. "
        + "Use simple language that a child can understand.]\n\n"
        + "**Sample Sentences:**\n"
        + "1. [A short example sentence using \"" + word + "\"]\n"
        + "2. [Another short example sentence using \"" + word + "\"]\n\n"
        + "**繁體中文翻譯 (Traditional Chinese):**\n"
        + "[Translate the article and both sample sentences into Traditional Chinese]\n\n"
        + "**日本語訳 (Japanese):**\n"
        + "[Translate the article and both sample sentences into Japanese. "
        + "For every kanji, add the reading in hiragana in parentheses immediately after it, like: 日本語(にほんご)]\n\n"
        + "Follow this format exactly. Do not skip any section.";

    Map<String, Object> body = Map.of(
        "model", MODEL,
        "messages", List.of(
            Map.of("role", "system", "content", system),
            Map.of("role", "user", "content", user)
        ),
        "think", false,
        "temperature", 0.6,
        "stream", false
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));

    Map<String, Object> response = restTemplate.postForObject(
        LLM_URL, new HttpEntity<>(body, headers), Map.class);
    if (response == null) throw new RuntimeException("empty LLM response");

    Object message = response.get("message");
    if (message instanceof Map) {
      Object content = ((Map<String, Object>) message).get("content");
      if (content instanceof String) return (String) content;
    }
    Object resp = response.get("response");
    if (resp instanceof String) return (String) resp;
    throw new RuntimeException("unexpected LLM response format");
  }
}
