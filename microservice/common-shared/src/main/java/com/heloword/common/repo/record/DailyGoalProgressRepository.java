package com.heloword.common.repo.record;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.record.DailyGoalProgressEntity;

@Repository
public interface DailyGoalProgressRepository extends IBaseRepo<DailyGoalProgressEntity, Long> {

  Optional<DailyGoalProgressEntity> findByUserUuidAndDate(String userUuid, String date);
}
