package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

/** A sentence from an article the reader wants broken down. */
@Data
public class ExplainSentenceRequest {
  private String sentence;
  private String wordLang;
}
