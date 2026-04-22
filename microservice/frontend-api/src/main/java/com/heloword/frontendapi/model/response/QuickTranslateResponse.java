package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickTranslateResponse {
  private String word;
  private String wordLang;
  private String translateEn;
  private String translateCh;
}
