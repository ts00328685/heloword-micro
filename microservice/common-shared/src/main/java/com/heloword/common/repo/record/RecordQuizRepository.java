package com.heloword.common.repo.record;

import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.record.RecordQuizEntity;

@Repository
public interface RecordQuizRepository extends IBaseRepo<RecordQuizEntity, Long> {
  List<RecordQuizEntity> findAllByUsernameAndRecordQuizSettingIdIn(String username, List<Long> ids);
  List<RecordQuizEntity> findAllByUsernameAndFinishedTimeBetween(String username, Date from, Date to);

  @Modifying
  @Transactional
  @Query("DELETE FROM RecordQuizEntity r WHERE r.recordQuizSettingId IN :settingIds")
  void deleteAllByRecordQuizSettingIdIn(@Param("settingIds") List<Long> settingIds);
}
