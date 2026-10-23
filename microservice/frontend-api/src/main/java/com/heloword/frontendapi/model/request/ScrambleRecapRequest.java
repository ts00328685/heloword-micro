package com.heloword.frontendapi.model.request;

import java.util.List;
import lombok.Data;

/** Sentences the learner struggled with today, for a short grammar recap. */
@Data
public class ScrambleRecapRequest {
  /** "jp" or "en" */
  private String lang;
  private List<Item> items;

  @Data
  public static class Item {
    private String sentence;
    private String translation;
  }
}
