package com.heloword.frontendapi.rest.challenge;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import com.heloword.frontendapi.model.challenge.ChallengeAnswerDto;
import com.heloword.frontendapi.service.challenge.ChallengeService;

@Controller
@AllArgsConstructor
public class ChallengeMessageController {

  private final ChallengeService challengeService;

  @MessageMapping("/challenge/room/{roomId}/answer")
  public void handleAnswer(@DestinationVariable String roomId,
      @Payload ChallengeAnswerDto answer) {
    challengeService.processAnswer(roomId, answer);
  }
}
