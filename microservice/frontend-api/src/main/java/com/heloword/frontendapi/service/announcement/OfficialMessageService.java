package com.heloword.frontendapi.service.announcement;

import java.util.List;
import com.heloword.frontendapi.model.response.OfficialMessageDto;

public interface OfficialMessageService {
  List<OfficialMessageDto> getAll();
}
