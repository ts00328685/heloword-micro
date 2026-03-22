package com.heloword.frontendapi.service.challenge;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.heloword.frontendapi.model.challenge.ChallengeEventDto;
import com.heloword.frontendapi.model.challenge.ChallengeRoomDto;
import java.util.List;

@Service
@AllArgsConstructor
public class ChallengePushService {

  private final SimpMessagingTemplate messagingTemplate;

  public void broadcastRoomList(List<ChallengeRoomDto> rooms) {
    messagingTemplate.convertAndSend("/topic/challenge/rooms", rooms);
  }

  public void broadcastRoomEvent(String roomId, ChallengeEventDto event) {
    messagingTemplate.convertAndSend("/topic/challenge/room/" + roomId, event);
  }
}
