package com.heloword.frontendapi.service.notification;

import java.util.List;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.response.DueWordDto;

public interface NotificationService {
  List<DueWordDto> getDueForReview(UserDto userDto);
}
