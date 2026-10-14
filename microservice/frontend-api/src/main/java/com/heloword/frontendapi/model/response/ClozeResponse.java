package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Fill-in-the-blank sentence; {@code answer} is the exact (possibly inflected) form removed. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClozeResponse {
  private String sentence;
  private String answer;
  private String translation;
}
