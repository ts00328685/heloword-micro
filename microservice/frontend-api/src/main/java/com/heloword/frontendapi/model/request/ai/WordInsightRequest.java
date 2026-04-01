package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

@Data
public class WordInsightRequest {
  private String word;
  private String translateEn;
  private String translateCh;
  /** UI language (en/zh/ja) */
  private String lang;
  /** Word's own language (en/de/jp) — used to determine example sentence language */
  private String wordLang;
}
