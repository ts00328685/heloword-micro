package com.heloword.record.service;

import java.util.List;
import java.util.Map;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.record.RecordQuizEntity;

public interface RecordQuizService extends IBaseService<RecordQuizEntity, Long> {
  Map<Long, List<Long>> getAllRecordsBySettingIds(List<Long> settingIds, String username);
}
