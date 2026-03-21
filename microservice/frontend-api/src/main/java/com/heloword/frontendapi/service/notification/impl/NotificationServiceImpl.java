package com.heloword.frontendapi.service.notification.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.response.DueWordDto;
import com.heloword.frontendapi.service.notification.NotificationService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  /**
   * Ebbinghaus-inspired spaced repetition intervals indexed by correct-review count.
   * correctCount 0 or 1 → review again tomorrow; 2 → 3 days; 3 → 7 days; etc.
   */
  private static final long[] INTERVAL_MS = {
      TimeUnit.DAYS.toMillis(1),   // 0 correct reviews
      TimeUnit.DAYS.toMillis(1),   // 1 correct review
      TimeUnit.DAYS.toMillis(3),   // 2 correct reviews
      TimeUnit.DAYS.toMillis(7),   // 3 correct reviews
      TimeUnit.DAYS.toMillis(14),  // 4 correct reviews
      TimeUnit.DAYS.toMillis(30),  // 5 correct reviews
      TimeUnit.DAYS.toMillis(90),  // 6+ correct reviews
  };

  private ServiceRecordClient serviceRecordClient;

  @Override
  public List<DueWordDto> getDueForReview(UserDto userDto) {
    List<RecordQuizEntity> allRecords = serviceRecordClient
        .getRecordsByDateRange(userDto.getUsername(), 0L, System.currentTimeMillis())
        .getData();

    long now = System.currentTimeMillis();

    // Group by (answerId, answerTableName) — unique word key
    Map<String, List<RecordQuizEntity>> byWord = allRecords.stream()
        .filter(r -> r.getFinishedTime() != null)
        .collect(Collectors.groupingBy(r -> r.getAnswerId() + "|" + r.getAnswerTableName()));

    return byWord.values().stream()
        .map(records -> buildDueWord(records, now))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private DueWordDto buildDueWord(List<RecordQuizEntity> records, long now) {
    // Latest review time
    Date lastReviewTime = records.stream()
        .map(RecordQuizEntity::getFinishedTime)
        .filter(Objects::nonNull)
        .max(Date::compareTo)
        .orElse(null);

    if (lastReviewTime == null) return null;

    int reviewCount  = records.size();
    int correctCount = (int) records.stream()
        .filter(r -> r.getWrongCount() == null || r.getWrongCount() == 0)
        .count();

    int intervalIndex = Math.min(correctCount, INTERVAL_MS.length - 1);
    long nextReviewMs = lastReviewTime.getTime() + INTERVAL_MS[intervalIndex];

    // Only return words that are due now
    if (nextReviewMs > now) return null;

    RecordQuizEntity sample = records.get(0);
    return DueWordDto.builder()
        .answerId(sample.getAnswerId())
        .answerTableName(sample.getAnswerTableName())
        .lastReviewTime(lastReviewTime)
        .nextReviewTime(new Date(nextReviewMs))
        .reviewCount(reviewCount)
        .correctCount(correctCount)
        .build();
  }
}
