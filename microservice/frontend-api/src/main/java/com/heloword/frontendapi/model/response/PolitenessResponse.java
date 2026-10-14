package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One Japanese sentence in three registers. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolitenessResponse {
  private String casual;
  private String polite;
  private String keigo;
}
