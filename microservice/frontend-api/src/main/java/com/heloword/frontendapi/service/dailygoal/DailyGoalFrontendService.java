package com.heloword.frontendapi.service.dailygoal;

import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.request.DailyGoalProgressRequest;

public interface DailyGoalFrontendService {

  void save(UserDto userDto, DailyGoalProgressRequest request);
}
