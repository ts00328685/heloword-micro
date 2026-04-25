package com.heloword.record.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.record.DailyGoalProgressEntity;
import com.heloword.record.service.DailyGoalService;

@RestController
@RequestMapping("/daily-goal")
public class DailyGoalRestController {

  @Autowired
  private DailyGoalService dailyGoalService;

  @PostMapping("/save")
  public HeloResponse<?> save(@RequestBody DailyGoalProgressEntity entity) {
    return HeloResponse.successWithData(dailyGoalService.save(entity));
  }
}
