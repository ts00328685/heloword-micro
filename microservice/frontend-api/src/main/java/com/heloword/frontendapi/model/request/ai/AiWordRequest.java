package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

/** Generic single-word AI request (memory hook, cloze, collocations, level). */
@Data
public class AiWordRequest {
  private String word;
  private String wordLang;
  private String meaningZh;
}
