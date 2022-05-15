package com.heloword.frontendapi.service.home;

import java.util.Optional;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.response.DashboardResponse;

public interface DashboardService {
  DashboardResponse getDashboardResponse(Optional<UserDto> userDto);
}
