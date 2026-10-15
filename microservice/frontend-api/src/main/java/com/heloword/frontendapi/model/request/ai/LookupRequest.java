package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

/** A word tapped while reading, with the sentence it appeared in. */
@Data
public class LookupRequest {
  private String word;
  private String wordLang;
  private String sentence;
}
