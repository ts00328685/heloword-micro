package com.heloword.frontendapi.model.request;

import lombok.Data;

@Data
public class ScrambleAiRequest {
  /** Language code: "jp" or "en" */
  private String lang;
  /** The full sentence to explain */
  private String sentence;
  /** Chinese translation of the sentence (used as context in the prompt) */
  private String translation;
}
