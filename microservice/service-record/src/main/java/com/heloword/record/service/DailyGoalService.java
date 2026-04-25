package com.heloword.record.service;

import com.heloword.common.entity.record.DailyGoalProgressEntity;

public interface DailyGoalService {

  /** Upsert: insert or replace the daily progress record for the given user+date. */
  DailyGoalProgressEntity save(DailyGoalProgressEntity entity);
}
