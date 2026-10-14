package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A common word pairing and its Traditional Chinese meaning. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollocationDto {
  private String phrase;
  private String meaning;
}
