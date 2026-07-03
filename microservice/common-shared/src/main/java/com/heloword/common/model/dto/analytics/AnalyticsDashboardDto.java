package com.heloword.common.model.dto.analytics;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Everything the admin analytics dashboard needs, for a given day window. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsDashboardDto {
  private int days;
  private long totalEvents;
  private long uniqueUsers;
  private long pageViews;
  private long activeToday;
  private List<AnalyticsPointDto> daily;
  private List<AnalyticsCountDto> topPages;
  private List<AnalyticsCountDto> topEvents;
  private List<AnalyticsCountDto> topContent;
  private List<AnalyticsCountDto> devices;
  private List<AnalyticsCountDto> userTypes;
}
