package com.heloword.frontendapi.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A fresh scramble item using the same grammar pattern as the one just solved. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarSentenceResponse {
  private String sentence;
  /** The sentence split into chunks, in the correct order. */
  private List<String> chunks;
  /** Traditional Chinese translation */
  private String translation;
  /** English translation (Japanese items only; empty otherwise) */
  private String english;
}
