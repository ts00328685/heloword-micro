package com.heloword.frontendapi.model.request.ai;

import lombok.Data;

/** A Japanese sentence to rewrite in casual / polite / keigo. */
@Data
public class PolitenessRequest {
  private String sentence;
}
