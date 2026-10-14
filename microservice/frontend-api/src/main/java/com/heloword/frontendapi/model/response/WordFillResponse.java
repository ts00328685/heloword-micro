package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordFillResponse {
  private String translateEn;
  private String translateCh;
  private String sentence;
  /** JLPT N5–N1 or CEFR A1–C2; empty if the model gave no valid level */
  private String level;
}
