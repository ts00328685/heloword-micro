package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerbConjugationResponse {
  private String word;
  private String wordLang;
  private String meaningEn;
  private String meaningZh;
  /** Formatted conjugation table as a newline-delimited string. */
  private String conjugations;
}
