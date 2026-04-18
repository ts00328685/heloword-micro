package com.heloword.frontendapi.model.request;

import lombok.Data;

@Data
public class TranslationAnalyzeRequest {
  /** Language code: "jp" or "en" */
  private String lang;
  /** The correct answer sentence */
  private String answer;
  /** The user's written input */
  private String userInput;
  /** Chinese translation of the sentence (context for the AI) */
  private String translation;
}
