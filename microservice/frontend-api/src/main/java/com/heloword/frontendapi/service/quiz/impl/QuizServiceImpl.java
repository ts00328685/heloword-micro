package com.heloword.frontendapi.service.quiz.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    Stream<RecordQuizSettingDto> recordQuizSettingDtoStream = serviceRecordClient.getQuizSettings(userDto.getUsername()).getData()
        .parallelStream()
        .map(RecordQuizSettingDto::fromEntity);

    // setting id : finished_count
    Map<Long, Long> quizSettingFinishedCountMap = serviceRecordClient.getQuizSettingFinishedCount(userDto.getUsername()).getData();

    return recordQuizSettingDtoStream.map(record -> fillFinishedCount(record, quizSettingFinishedCountMap))
        .collect(groupingBy(RecordQuizSettingDto::getTimestamp, toList()));
  }

  private static RecordQuizSettingDto fillFinishedCount(RecordQuizSettingDto recordQuizSettingDto, Map<Long, Long> quizSettingFinishedCountMap) {
    recordQuizSettingDto.setFinishedCount(quizSettingFinishedCountMap.get(recordQuizSettingDto.getId()).intValue());
    return recordQuizSettingDto;
  }
}
