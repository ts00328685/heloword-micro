package com.heloword.frontendapi.model.challenge;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengePlayerDto {
  private String userId;
  private String displayName;
  private int score;
  private boolean isGuest;
}
