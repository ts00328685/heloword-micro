package com.heloword.record.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.entity.record.DailyGoalProgressEntity;
import com.heloword.common.repo.record.DailyGoalProgressRepository;
import com.heloword.record.service.DailyGoalService;

@Service
public class DailyGoalServiceImpl implements DailyGoalService {

  @Autowired
  private DailyGoalProgressRepository repository;

  @Override
  public DailyGoalProgressEntity save(DailyGoalProgressEntity incoming) {
    return repository.findByUserUuidAndDate(incoming.getUserUuid(), incoming.getDate())
        .map(existing -> {
          existing.setJpQuizWords(incoming.getJpQuizWords());
          existing.setJpScrambleSentences(incoming.getJpScrambleSentences());
          existing.setJpSpokenSentences(incoming.getJpSpokenSentences());
          existing.setJpSpokenScoreTotal(incoming.getJpSpokenScoreTotal());
          existing.setJpWrittenSentences(incoming.getJpWrittenSentences());
          existing.setEnQuizWords(incoming.getEnQuizWords());
          existing.setEnScrambleSentences(incoming.getEnScrambleSentences());
          existing.setEnSpokenSentences(incoming.getEnSpokenSentences());
          existing.setEnSpokenScoreTotal(incoming.getEnSpokenScoreTotal());
          existing.setEnWrittenSentences(incoming.getEnWrittenSentences());
          return repository.save(existing);
        })
        .orElseGet(() -> repository.save(incoming));
  }
}
