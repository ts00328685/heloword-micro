package com.heloword.frontendapi.config;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CacheConfig {

  public static final String DASHBOARD_CACHE = "dashboard";
  /**
   * LLM response cache — keyed by (wordLang, word).
   * Shared across word-insight and sample-sentence features.
   * 7-day TTL: vocabulary definitions are stable; large capacity covers top words.
   */
  public static final String AI_CACHE = "aiFeature";
  /** Random 5 fun-articles from DB, refreshed every hour. */
  public static final String FUN_ARTICLE_CACHE = "funArticle";

  @Bean
  public CacheManager cacheManager() {
    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(Arrays.asList(
        buildCache(DASHBOARD_CACHE, 24, TimeUnit.HOURS, 1),
        buildCache(AI_CACHE, 7, TimeUnit.DAYS, 2000),
        buildCache(FUN_ARTICLE_CACHE, 1, TimeUnit.HOURS, 1)
    ));
    return manager;
  }

  private CaffeineCache buildCache(String name, long duration, TimeUnit unit, long maxSize) {
    return new CaffeineCache(name,
        Caffeine.newBuilder()
            .expireAfterWrite(duration, unit)
            .maximumSize(maxSize)
            .build());
  }
}
