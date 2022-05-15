package com.heloword.frontendapi.service.quiz;

import java.util.List;
import com.heloword.common.model.dto.RecordQuizDto;
import com.heloword.common.model.dto.RecordQuizSettingDto;
import com.heloword.common.model.dto.UserDto;

public interface QuizService {
  void saveQuizRecord(UserDto userDto, RecordQuizDto recordQuizDto);
  List<Long> saveAllQuizSettingRecord(UserDto userDto, List<RecordQuizSettingDto> recordQuizSettingDto);
}
