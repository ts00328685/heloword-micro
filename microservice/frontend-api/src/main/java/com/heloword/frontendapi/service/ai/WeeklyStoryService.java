package com.heloword.frontendapi.service.ai;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.heloword.common.base.entity.BaseWordEntity;
import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.response.DashboardResponse;
import com.heloword.frontendapi.model.response.WeeklyStoryResponse;
import com.heloword.frontendapi.service.home.DashboardService;

/**
 * Weekly mini-story from a member's most-missed words.
 *
 * <p>Generated at most once per member per ISO week and stored in Redis, so opening the
 * card again is free and costs no quota. Words come from the member's own quiz records on
 * the server — the client can't choose what goes into the prompt.
 */
@Log4j2
@Service
public class WeeklyStoryService {

  private static final ZoneId ZONE = ZoneId.of("Asia/Taipei");
  private static final int MIN_WORDS = 3;
  private static final int MAX_WORDS = 5;

  @Autowired private ServiceRecordClient serviceRecordClient;
  @Autowired private DashboardService dashboardService;
  @Autowired private AiFeatureService aiFeatureService;
  @Autowired private AiGuard aiGuard;
  @Autowired private StringRedisTemplate redis;

  private final ObjectMapper objectMapper = new ObjectMapper();

  /** @return the story, or a response with empty story when there aren't enough missed words yet */
  public WeeklyStoryResponse get(UserDto user) {
    LocalDate today = LocalDate.now(ZONE);
    String week = today.get(IsoFields.WEEK_BASED_YEAR) + "-W" + today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    String key = "ai:story:" + user.getUsername() + ":" + week;

    String cached = redis.opsForValue().get(key);
    if (cached != null) {
      try {
        return objectMapper.readValue(cached, WeeklyStoryResponse.class);
      } catch (Exception e) {
        log.warn("Corrupt weekly story cache for {}, regenerating", user.getUsername());
      }
    }

    List<BaseWordEntity> missed = topMissedWords(user.getUsername());
    if (missed.size() < MIN_WORDS) {
      return new WeeklyStoryResponse(week, missed.stream().map(BaseWordEntity::getWord).collect(Collectors.toList()), "", "");
    }

    // Keep to one language so the story reads naturally.
    String wordLang = Optional.ofNullable(missed.get(0).getLanguage()).orElse("en");
    List<String> words = missed.stream()
        .filter(w -> wordLang.equals(Optional.ofNullable(w.getLanguage()).orElse("en")))
        .map(BaseWordEntity::getWord)
        .limit(MAX_WORDS)
        .collect(Collectors.toList());

    String[] story = aiGuard.run(() -> aiFeatureService.weeklyStory(words, wordLang));
    WeeklyStoryResponse response = new WeeklyStoryResponse(week, words, story[0], story[1]);

    try {
      redis.opsForValue().set(key, objectMapper.writeValueAsString(response), Duration.ofDays(8));
    } catch (Exception e) {
      log.warn("Failed to cache weekly story for {}: {}", user.getUsername(), e.getMessage());
    }
    return response;
  }

  /** Words with the most wrong answers in the last 7 days, most-missed first. */
  private List<BaseWordEntity> topMissedWords(String username) {
    long now = System.currentTimeMillis();
    List<RecordQuizEntity> records = Optional.ofNullable(
        serviceRecordClient.getRecordsByDateRange(username, now - Duration.ofDays(7).toMillis(), now).getData())
        .orElse(List.of());

    Map<String, Integer> wrongByWord = new HashMap<>();
    for (RecordQuizEntity r : records) {
      if (r.getWrongCount() == null || r.getWrongCount() <= 0 || r.getAnswerId() == null) continue;
      wrongByWord.merge(key(r.getAnswerId(), r.getAnswerTableName()), r.getWrongCount(), Integer::sum);
    }
    if (wrongByWord.isEmpty()) return List.of();

    // System vocabulary only — custom words live per group and aren't in the shared lists.
    DashboardResponse all = dashboardService.getDashboardResponse(Optional.empty());
    Map<String, BaseWordEntity> byKey = Stream.of(
            all.getWordEnglishList(), all.getWordJapaneseList(), all.getWordJapaneseVerbList(), all.getWordGermanList())
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .collect(Collectors.toMap(w -> key(w.getId(), w.getTableName()), w -> (BaseWordEntity) w, (a, b) -> a));

    return wrongByWord.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .map(e -> byKey.get(e.getKey()))
        .filter(w -> w != null && w.getWord() != null && !w.getWord().isBlank())
        .limit(MAX_WORDS * 2L)
        .collect(Collectors.toList());
  }

  private static String key(Long id, String tableName) {
    return id + "|" + (tableName == null ? "" : tableName.toLowerCase());
  }
}
