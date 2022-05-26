package com.heloword.record.service;

import java.util.List;
import java.util.Map;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.record.RecordQuizSettingEntity;

public interface RecordQuizSettingService extends IBaseService<RecordQuizSettingEntity, Long> {
  List<Map<String, Number>> getQuizSettingFinishedCount(String username);
}
