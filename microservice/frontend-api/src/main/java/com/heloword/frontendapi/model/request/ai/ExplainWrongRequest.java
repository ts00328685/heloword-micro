package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

/** A wrong multiple-choice pick to explain. */
@Data
public class ExplainWrongRequest {
  private String word;
  private String wordLang;
  private String picked;
  private String answer;
}
