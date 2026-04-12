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
  private static final String MODEL = "gemma4:e2b";
  private static final int ARTICLE_COUNT = 5;

  @Autowired
  private ServiceWordClient serviceWordClient;

  @Autowired
  private ServiceRecordClient serviceRecordClient;

  /** Self-injection through proxy so getLatest() benefits from getAll() cache. */
  @Autowired
  @Lazy
  private FunArticleService self;

  private final RestTemplate restTemplate = new RestTemplate();

  /** Runs at startup then every 6 hours. Generates articles and persists them via service-record. */
  @Scheduled(fixedDelay = 6 * 60 * 60 * 1000L)
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
  @Cacheable(value = CacheConfig.FUN_ARTICLE_CACHE, key = "'all'")
  public List<FunArticleDto> getAll() {
    List<FunArticleEntity> entities = serviceRecordClient.getRandomFunArticles().getData();
    if (entities == null) return Collections.emptyList();
    return entities.stream()
        .map(e -> new FunArticleDto(e.getWord(), e.getContent()))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private String generateArticle(String word) {
    String system = "You are a funny American comedian who is also a traditional Chinese teacher "
        + "who explains things in an easy way that a kindergarten would understand.";
    String user = "Please use the word: " + word + " to write a 100 words short article that is funny "
        + "and helps people to memorize the word: " + word + ", please also give the traditional chinese "
        + "and japanese translation of the whole article below, for japanese, please put hirakana in "
        + "parenthesis next to every kanji word in the article, thanks.";

    Map<String, Object> body = Map.of(
        "model", MODEL,
        "messages", List.of(
            Map.of("role", "system", "content", system),
            Map.of("role", "user", "content", user)
        ),
        "think", true,
        "temperature", 0.8,
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
