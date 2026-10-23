package com.heloword.frontendapi.model.request;

import lombok.Data;

/** Ask for a grammar nudge toward the next chunk — never the chunk itself. */
@Data
public class ScrambleHintRequest {
  /** "jp" or "en" */
  private String lang;
  /** The correct sentence */
  private String sentence;
  /** Chunks already placed correctly, in order, joined with " / " (empty at the start) */
  private String placed;
  /** Chinese translation (context) */
  private String translation;
}
