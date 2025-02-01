package com.heloword.frontendapi.service.quiz.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.heloword.common.base.entity.BaseEntity;
import org.springframework.stereotype.Service;
import com.heloword.common.entity.record.RecordQuizSettingEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.RecordQuizDto;
import com.heloword.common.model.dto.RecordQuizSettingDto;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.service.quiz.QuizService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Log4j2
@Service
@AllArgsConstructor
public class QuizServiceImpl implements QuizService {

  private ServiceRecordClient serviceRecordClient;

  @Override
  public void saveQuizRecord(UserDto userDto, RecordQuizDto recordQuizDto) {
    recordQuizDto.setUsername(userDto.getUsername());
    serviceRecordClient.saveQuizRecord(RecordQuizDto.toEntity(recordQuizDto));
  }

  @Override
  public List<Long> saveAllQuizSettingRecord(UserDto userDto, List<RecordQuizSettingDto> recordQuizSettingDto) {
    List<RecordQuizSettingEntity> entities = recordQuizSettingDto.stream().map(dto -> {
      dto.setUsername(userDto.getUsername());
      return RecordQuizSettingDto.toEntity(dto);
    }).collect(Collectors.toList());
    return serviceRecordClient.saveAllQuizSettingRecord(entities).getData().stream().map(RecordQuizSettingEntity::getId).collect(Collectors.toList());
  }

  @Override
  public Map<Date, List<RecordQuizSettingDto>> getQuizSettings(UserDto userDto) {
    List<RecordQuizSettingEntity> allSettings = serviceRecordClient.getQuizSettings(userDto.getUsername()).getData();
    Stream<RecordQuizSettingDto> recordQuizSettingDtoStream = allSettings
        .parallelStream()
        .map(RecordQuizSettingDto::fromEntity);

    // setting id : finished_count
    Map<Long, Long> quizSettingFinishedCountMap = serviceRecordClient.getQuizSettingFinishedCount(userDto.getUsername()).getData();
    Map<Long, Date> quizSettingAnyLatestFinishedTimeMap = serviceRecordClient.getLatestFinishedTimeBySettingIds(
            userDto.getUsername(),
            allSettings.stream().map(BaseEntity::getId).collect(toList())
    ).getData();
    return recordQuizSettingDtoStream
            .map(record -> fillFinishedCountAndLatestFinishedTime(record, quizSettingFinishedCountMap, quizSettingAnyLatestFinishedTimeMap))
        .collect(groupingBy(RecordQuizSettingDto::getTimestamp, toList()));
  }

  @Override
  public Map<Long, List<Long>> getRecordIdsBySettingIds(UserDto userDto, List<Long> settingIds) {
    return serviceRecordClient.getRecordIdsBySettingIds(userDto.getUsername(), settingIds).getData();
  }


  private static RecordQuizSettingDto fillFinishedCountAndLatestFinishedTime(RecordQuizSettingDto recordQuizSettingDto, Map<Long, Long> quizSettingFinishedCountMap, Map<Long, Date> quizSettingAnyLatestFinishedTimeMap) {
    recordQuizSettingDto.setFinishedCount(quizSettingFinishedCountMap.get(recordQuizSettingDto.getId()).intValue());
    recordQuizSettingDto.setLatestFinishedTime(quizSettingAnyLatestFinishedTimeMap.get(recordQuizSettingDto.getId()));
    return recordQuizSettingDto;
  }
}
