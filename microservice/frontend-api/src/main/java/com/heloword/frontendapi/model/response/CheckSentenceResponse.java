package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result of checking a learner sentence; {@code fix} is empty when correct. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckSentenceResponse {
  private boolean ok;
  private String fix;
}
