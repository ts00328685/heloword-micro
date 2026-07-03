package com.heloword.record.service;

import java.util.List;
import com.heloword.common.model.dto.analytics.AnalyticsDashboardDto;
import com.heloword.common.model.dto.analytics.AnalyticsEventDto;

public interface AnalyticsService {

  /** Persist a batch of events. UUID-only; never throws for a bad element. */
  void ingest(List<AnalyticsEventDto> events);

  /** Aggregate the last {@code days} days of traffic for the admin dashboard. */
  AnalyticsDashboardDto getDashboard(int days);
}
