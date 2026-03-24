package com.heloword.frontendapi.service.quiz;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.heloword.common.entity.record.RecordQuizGroupOverrideEntity;
import com.heloword.common.model.dto.RecordQuizDto;
import com.heloword.common.model.dto.RecordQuizSettingDto;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.request.GroupOverrideRequest;

public interface QuizService {
  void saveQuizRecord(UserDto userDto, RecordQuizDto recordQuizDto);
  List<Long> saveAllQuizSettingRecord(UserDto userDto, List<RecordQuizSettingDto> recordQuizSettingDto);
  Map<Date, List<RecordQuizSettingDto>> getQuizSettings(UserDto userDto);
  Map<Long, List<Long>> getRecordIdsBySettingIds(UserDto userDto, List<Long> settingIds);
  void deleteGroup(UserDto userDto, String type, int min, int max);
  List<RecordQuizGroupOverrideEntity> getGroupOverrides(UserDto userDto);
  RecordQuizGroupOverrideEntity saveGroupOverride(UserDto userDto, GroupOverrideRequest request);
  void deleteGroupOverride(UserDto userDto, String groupKey);
}
