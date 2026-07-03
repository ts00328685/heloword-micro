package com.heloword.common.model.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One day in the traffic time-series: total events and unique visitors. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsPointDto {
  private String date;
  private long events;
  private long users;
}
