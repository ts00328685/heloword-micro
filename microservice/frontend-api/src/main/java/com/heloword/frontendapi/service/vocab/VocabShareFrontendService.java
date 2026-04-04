package com.heloword.frontendapi.service.vocab;

import java.util.List;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.model.dto.VocabShareRequestDto;

public interface VocabShareFrontendService {

  VocabShareRequestDto sendShare(UserDto user, VocabShareRequestDto dto);

  List<VocabShareRequestDto> getInbox(UserDto user);

  VocabShareRequestDto acceptShare(UserDto user, Long id);

  void rejectShare(UserDto user, Long id);
}
