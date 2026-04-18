package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

@Data
public class WordCompareRequest {
  private String word;
  private String translateEn;
  private String translateCh;
  /** UI language (en/zh/ja) */
  private String lang;
  /** Word's own language (en/de/jp) — used to determine comparison language */
  private String wordLang;
}
