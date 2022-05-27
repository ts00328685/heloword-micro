package com.heloword.frontendapi.rest.quiz;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.model.dto.RecordQuizDto;
import com.heloword.common.model.dto.RecordQuizSettingDto;
import com.heloword.frontendapi.service.quiz.QuizService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/quiz")
@AllArgsConstructor
public class QuizRestController extends AbstractBaseFrontendRestController {

  private QuizService quizService;

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/save-single-record")
  public HeloResponse<?> saveQuizRecord(@RequestBody RecordQuizDto recordQuizDto) {
    quizService.saveQuizRecord(getUser().get(), recordQuizDto);
    return HeloResponse.successWithoutData();
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/save-setting-records")
  public HeloResponse<?> saveQuizSettingRecord(@RequestBody List<RecordQuizSettingDto> recordQuizSettingDtos) {
    return HeloResponse.successWithData(
        Map.of("ids", quizService.saveAllQuizSettingRecord(getUser().get(), recordQuizSettingDtos))
    );
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/get-quiz-settings")
  public HeloResponse<?> getQuizSettings() {
    return HeloResponse.successWithData(quizService.getQuizSettings(getUser().get()));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/get-record-ids-by-setting-ids")
  public HeloResponse<?> getRecordIdsBySettingIds(@RequestBody List<Long> settingIds) {
    Map<Long, List<Long>> recordIdsBySettingIds = quizService.getRecordIdsBySettingIds(getUser().get(), settingIds);
    return HeloResponse.successWithData(quizService.getRecordIdsBySettingIds(getUser().get(), settingIds));
  }
}
