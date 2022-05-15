package com.heloword.record.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.record.RecordQuizSettingEntity;
import com.heloword.common.repo.record.RecordQuizSettingRepository;
import com.heloword.record.service.RecordQuizSettingService;

@Service
public class RecordQuizSettingServiceImpl extends AbstractBaseServiceImpl<RecordQuizSettingEntity, Long> implements RecordQuizSettingService {

  @Autowired
  private RecordQuizSettingRepository recordQuizSettingRepository;

  @Override
  protected RecordQuizSettingRepository getRepo() {
    return this.recordQuizSettingRepository;
  }

}
