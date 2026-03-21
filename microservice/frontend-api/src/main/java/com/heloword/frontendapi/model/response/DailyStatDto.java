package com.heloword.frontendapi.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyStatDto {
  /** Date string in yyyy-MM-dd format */
  private String date;
  private int total;
  private int wrongCount;
  /** Total time spent in seconds */
  private int timeSpent;
}
