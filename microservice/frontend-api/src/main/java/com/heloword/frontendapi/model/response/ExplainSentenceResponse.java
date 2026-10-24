package com.heloword.frontendapi.model.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A sentence broken into meaningful chunks (in order) with what each does, the grammar points
 * worth knowing, and a natural Traditional Chinese translation. Japanese text uses 漢字[かな].
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplainSentenceResponse {
  private List<Part> chunks;
  private List<Part> points;
  private String translation;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Part {
    private String text;
    private String note;
  }
}
