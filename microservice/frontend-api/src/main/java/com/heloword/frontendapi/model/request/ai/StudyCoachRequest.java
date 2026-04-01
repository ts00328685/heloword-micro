package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

@Data
public class StudyCoachRequest {
  private int accuracyPct;
  private int wrongCount;
  private String lang;
}
