package com.heloword.frontendapi.model.request;

import lombok.Data;

/** A full but wrongly ordered scramble answer, to explain what went wrong. */
@Data
public class ScrambleWrongOrderRequest {
  /** "jp" or "en" */
  private String lang;
  /** The correct sentence */
  private String sentence;
  /** The learner's order, chunks joined with " / " */
  private String attempt;
  /** Chinese translation (context) */
  private String translation;
}
