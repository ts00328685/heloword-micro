package com.heloword.frontendapi.service.vocab.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.model.dto.VocabShareRequestDto;
import com.heloword.frontendapi.service.social.SocialPushService;
import com.heloword.frontendapi.service.vocab.VocabShareFrontendService;

@Log4j2
@Service
@AllArgsConstructor
public class VocabShareFrontendServiceImpl implements VocabShareFrontendService {

  private ServiceRecordClient serviceRecordClient;
  private SocialPushService socialPushService;

  @Override
  public VocabShareRequestDto sendShare(UserDto user, VocabShareRequestDto dto) {
    dto.setFromUsername(user.getUsername());
    VocabShareRequestDto saved = serviceRecordClient.sendVocabShare(user.getUsername(), dto).getData();
    // Push real-time notification — use recipient's username as topic id
    try {
      socialPushService.sendVocabShareToUser(saved.getToUsername(), saved);
    } catch (Exception e) {
      log.warn("Failed to push vocab-share notification: {}", e.getMessage());
    }
    return saved;
  }

  @Override
  public List<VocabShareRequestDto> getInbox(UserDto user) {
    return serviceRecordClient.getVocabShareInbox(user.getUsername()).getData();
  }

  @Override
  public VocabShareRequestDto acceptShare(UserDto user, Long id) {
    return serviceRecordClient.acceptVocabShare(user.getUsername(), id).getData();
  }

  @Override
  public void rejectShare(UserDto user, Long id) {
    serviceRecordClient.rejectVocabShare(user.getUsername(), id);
  }
}
