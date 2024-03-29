package com.heloword.common.feignclient;

import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.common.entity.record.RecordQuizSettingEntity;
import com.heloword.common.filter.FeignClientInterceptor;

@FeignClient(name = "SERVICE-RECORD", url = "${feign.service-record.url:}", configuration = FeignClientInterceptor.class)
public interface ServiceRecordClient {

  @PostMapping("/api/record-quiz")
  HeloResponse<RecordQuizEntity> saveQuizRecord(RecordQuizEntity recordQuizEntity);

  @PostMapping("/api/record-quiz/get-by-setting-ids")
  HeloResponse<Map<Long, List<Long>>> getRecordIdsBySettingIds(@RequestHeader String username, @RequestBody List<Long> settingIds);

  @PutMapping("/api/record-quiz-setting")
  HeloResponse<List<RecordQuizSettingEntity>> saveAllQuizSettingRecord(List<RecordQuizSettingEntity> recordQuizSettingEntity);

  @GetMapping("/api/record-quiz-setting/get-quiz-settings")
  HeloResponse<List<RecordQuizSettingEntity>> getQuizSettings(@RequestHeader String username);

  @GetMapping("/api/record-quiz-setting/get-finished-count")
  HeloResponse<Map<Long, Long>> getQuizSettingFinishedCount(@RequestHeader String username);

}
