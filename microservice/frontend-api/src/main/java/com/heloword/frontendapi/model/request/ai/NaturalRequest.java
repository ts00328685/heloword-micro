package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

/** A textbook-sounding English or Japanese phrase to rephrase the way natives say it. */
@Data
public class NaturalRequest {
  private String phrase;
  /** Set by the controller from the phrase itself ("jp" or "en"); not trusted from the client. */
  private String lang;
}
