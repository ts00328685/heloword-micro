package com.heloword.common.repo.record;

import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.record.RecordQuizEntity;

@Repository
public interface RecordQuizRepository extends IBaseRepo<RecordQuizEntity, Long> {
  List<RecordQuizEntity> findAllByUsernameAndRecordQuizSettingIdIn(String username, List<Long> ids);
  List<RecordQuizEntity> findAllByUsernameAndFinishedTimeBetween(String username, Date from, Date to);
}
