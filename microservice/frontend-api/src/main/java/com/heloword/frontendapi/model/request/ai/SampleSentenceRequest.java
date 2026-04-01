package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

@Data
public class SampleSentenceRequest {
  private String word;
  private String translateEn;
  /** UI language (en/zh/ja) */
  private String lang;
  /** Word's own language (en/de/jp) — used to determine example sentence language */
  private String wordLang;
}
