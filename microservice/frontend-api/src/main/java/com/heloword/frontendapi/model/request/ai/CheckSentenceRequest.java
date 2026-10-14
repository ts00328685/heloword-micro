package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

/** A sentence the learner wrote using {@code word}. */
@Data
public class CheckSentenceRequest {
  private String word;
  private String wordLang;
  private String sentence;
}
