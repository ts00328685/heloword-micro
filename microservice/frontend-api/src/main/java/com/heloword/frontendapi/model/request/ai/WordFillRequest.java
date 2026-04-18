package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

@Data
public class WordFillRequest {
  private String word;
  /** Word's own language code (e.g. ja, en, de, ko, zh) */
  private String wordLang;
}
