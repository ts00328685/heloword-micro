package com.heloword.common.repo.record;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.record.RecordQuizGroupOverrideEntity;

@Repository
public interface RecordQuizGroupOverrideRepository extends IBaseRepo<RecordQuizGroupOverrideEntity, Long> {

  List<RecordQuizGroupOverrideEntity> findAllByUsername(String username);

  Optional<RecordQuizGroupOverrideEntity> findByUsernameAndGroupKey(String username, String groupKey);

  void deleteByUsernameAndGroupKey(String username, String groupKey);
}
