package com.heloword.frontendapi.model.challenge;

import lombok.Data;

@Data
public class ChallengeAnswerDto {
  private String userId;
  private String displayName;
  private boolean guest;
  private String answer;
  private String questionId;
}
