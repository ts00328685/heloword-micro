package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Meaning of a word as used in its sentence; {@code word} is the dictionary form (Japanese with 漢字[かな]). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LookupResponse {
  private String word;
  private String meaningZh;
  private String meaningEn;
}
