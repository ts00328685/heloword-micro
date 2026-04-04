package com.heloword.record.service;

import java.util.List;
import com.heloword.common.model.dto.VocabShareRequestDto;

public interface VocabShareService {

  VocabShareRequestDto sendShare(String fromUsername, VocabShareRequestDto dto);

  List<VocabShareRequestDto> getInbox(String toUsername);

  VocabShareRequestDto acceptShare(Long id, String toUsername);

  void rejectShare(Long id, String toUsername);
}
