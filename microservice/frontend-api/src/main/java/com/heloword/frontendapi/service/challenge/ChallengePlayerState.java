package com.heloword.frontendapi.service.challenge;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengePlayerState {
  private String userId;
  private String displayName;
  private int score;
  private boolean guest;
}
