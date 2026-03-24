package com.heloword.common.repo.record;

import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.record.RecordQuizSettingEntity;

@Repository
public interface RecordQuizSettingRepository extends IBaseRepo<RecordQuizSettingEntity, Long> {

  @Modifying
  @Transactional
  @Query("DELETE FROM RecordQuizSettingEntity s WHERE s.id IN :ids")
  void deleteAllByIdIn(@Param("ids") List<Long> ids);

  @Query(nativeQuery = true, value =
      "select rqs.id, count(rq.id) as finished_count "
          + " from record_quiz_setting rqs "
          + " left join record_quiz rq on rqs.id = rq.record_quiz_setting_id "
          + " where rqs.username = :username "
          + " group by rqs.id")
  List<Map<String, Number>> getQuizSettingFinishedCount(@Param("username") String username);

}
