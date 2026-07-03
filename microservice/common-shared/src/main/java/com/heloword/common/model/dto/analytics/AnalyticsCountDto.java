package com.heloword.common.model.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A named bucket with a count (top pages / top events / device / user-type splits). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsCountDto {
  private String name;
  private long count;
}
