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
}
