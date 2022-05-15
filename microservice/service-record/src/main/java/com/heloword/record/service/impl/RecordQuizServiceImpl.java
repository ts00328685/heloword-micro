package com.heloword.record.service.impl;

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

}
