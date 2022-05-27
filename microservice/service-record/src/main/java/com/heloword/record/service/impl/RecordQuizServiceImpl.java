package com.heloword.record.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.common.repo.record.RecordQuizRepository;
import com.heloword.record.service.RecordQuizService;

@Service
public class RecordQuizServiceImpl extends AbstractBaseServiceImpl<RecordQuizEntity, Long> implements RecordQuizService {

  @Autowired
  private RecordQuizRepository recordQuizRepository;

  @Override
  protected RecordQuizRepository getRepo() {
    return this.recordQuizRepository;
  }

  public List<RecordQuizEntity> getAllRecordsBySettingIds(List<Long> settingIds) {
    return this.recordQuizRepository.findAllByRecordQuizSettingIdIn(settingIds);
  }

}
