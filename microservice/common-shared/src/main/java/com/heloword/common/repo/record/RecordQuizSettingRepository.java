package com.heloword.common.repo.record;

import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.record.RecordQuizSettingEntity;

@Repository
public interface RecordQuizSettingRepository extends IBaseRepo<RecordQuizSettingEntity, Long> {

  @Query(nativeQuery = true, value =
      "select rqs.id, count(*) as finished_count "
          + "from record_quiz rq "
          + "join record_quiz_setting rqs on rqs.id = rq.record_quiz_setting_id "
          + "where rqs.username = :username "
          + "group by rqs.id, rq.answer_table_name")
  List<Map<String, Number>> getQuizSettingFinishedCount(@Param("username") String username);

}
