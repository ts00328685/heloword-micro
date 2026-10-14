package com.heloword.frontendapi.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Short story built from the member's most-missed words this week. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyStoryResponse {
  private String week;
  private java.util.List<String> words;
  private String story;
  private String translation;
}
