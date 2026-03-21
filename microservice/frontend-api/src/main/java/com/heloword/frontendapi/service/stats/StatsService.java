package com.heloword.frontendapi.service.stats;

import java.util.List;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.response.DailyStatDto;

public interface StatsService {
  /** @param days 7, 30, or 0 for all-time (grouped by month) */
  List<DailyStatDto> getDailySummary(UserDto userDto, int days);
}
