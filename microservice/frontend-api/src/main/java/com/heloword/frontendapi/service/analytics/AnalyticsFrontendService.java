package com.heloword.frontendapi.service.analytics;

import java.util.List;
import com.heloword.common.model.dto.analytics.AnalyticsDashboardDto;
import com.heloword.common.model.dto.analytics.AnalyticsEventDto;

public interface AnalyticsFrontendService {

  /** Forward a batch to service-record. Best-effort — never throws to the caller. */
  void ingest(List<AnalyticsEventDto> events);

  AnalyticsDashboardDto getDashboard(int days);
}
