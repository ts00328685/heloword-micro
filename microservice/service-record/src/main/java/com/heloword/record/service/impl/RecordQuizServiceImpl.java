package com.heloword.record.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.common.repo.record.RecordQuizRepository;
import com.heloword.record.service.RecordQuizService;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
public class RecordQuizServiceImpl extends AbstractBaseServiceImpl<RecordQuizEntity, Long> implements RecordQuizService {

  @Autowired
  private RecordQuizRepository recordQuizRepository;

  @Override
  protected RecordQuizRepository getRepo() {
    return this.recordQuizRepository;
  }

  public Map<Long, List<Long>> getAllRecordsBySettingIds(List<Long> settingIds, String username) {
    return this.recordQuizRepository.findAllByUsernameAndRecordQuizSettingIdIn(username, settingIds).stream()
        .collect(groupingBy(RecordQuizEntity::getRecordQuizSettingId, mapping(RecordQuizEntity::getAnswerId, toList())));
  }

}
