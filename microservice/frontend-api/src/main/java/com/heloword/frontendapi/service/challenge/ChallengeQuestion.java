package com.heloword.frontendapi.service.challenge;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeQuestion {
  private String id;
  private String question;       // the clue shown to players (translateCh or translateEn)
  private String normalizedAnswer; // lowercase trimmed expected answer
  private String displayAnswer;  // original form for showing after round
}
