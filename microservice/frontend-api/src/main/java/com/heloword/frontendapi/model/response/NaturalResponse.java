package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** How natives actually say a phrase: an everyday version, a casual one, and when to use which. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaturalResponse {
  private String lang;
  private String natural;
  private String casual;
  /** One sentence in Traditional Chinese comparing the original, natural and casual versions. */
  private String note;
}
