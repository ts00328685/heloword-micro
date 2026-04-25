package com.heloword.frontendapi.service.dailygoal.impl;

import org.springframework.stereotype.Service;
import com.heloword.common.entity.record.DailyGoalProgressEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.request.DailyGoalProgressRequest;
import com.heloword.frontendapi.service.dailygoal.DailyGoalFrontendService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DailyGoalFrontendServiceImpl implements DailyGoalFrontendService {

  private ServiceRecordClient serviceRecordClient;

  @Override
  public void save(UserDto userDto, DailyGoalProgressRequest request) {
    DailyGoalProgressRequest.LangProgress jp = request.getJapanese() != null
        ? request.getJapanese()
        : new DailyGoalProgressRequest.LangProgress();
    DailyGoalProgressRequest.LangProgress en = request.getEnglish() != null
        ? request.getEnglish()
        : new DailyGoalProgressRequest.LangProgress();

    DailyGoalProgressEntity entity = DailyGoalProgressEntity.builder()
        .userUuid(userDto.getUuid())
        .username(userDto.getUsername())
        .date(request.getDate())
        .jpQuizWords(jp.getQuizWords())
        .jpScrambleSentences(jp.getScrambleSentences())
        .jpSpokenSentences(jp.getSpokenSentences())
        .jpSpokenScoreTotal(jp.getSpokenScoreTotal())
        .jpWrittenSentences(jp.getWrittenSentences())
        .enQuizWords(en.getQuizWords())
        .enScrambleSentences(en.getScrambleSentences())
        .enSpokenSentences(en.getSpokenSentences())
        .enSpokenScoreTotal(en.getSpokenScoreTotal())
        .enWrittenSentences(en.getWrittenSentences())
        .build();

    serviceRecordClient.saveDailyGoalProgress(entity);
  }
}
