package com.heloword.frontendapi.service.analytics.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.analytics.AnalyticsDashboardDto;
import com.heloword.common.model.dto.analytics.AnalyticsEventDto;
import com.heloword.common.model.dto.analytics.AnalyticsIngestDto;
import com.heloword.frontendapi.service.analytics.AnalyticsFrontendService;

@Log4j2
@Service
@AllArgsConstructor
public class AnalyticsFrontendServiceImpl implements AnalyticsFrontendService {

  private final ServiceRecordClient serviceRecordClient;

  @Override
  public void ingest(List<AnalyticsEventDto> events) {
    if (events == null || events.isEmpty()) {
      return;
    }
    try {
      AnalyticsIngestDto body = new AnalyticsIngestDto();
      body.setEvents(events);
      serviceRecordClient.ingestAnalytics(body);
    } catch (Exception ex) {
      // Analytics is best-effort: swallow so a downstream hiccup never affects the user.
      log.warn("analytics ingest failed (ignored): {}", ex.getMessage());
    }
  }

  @Override
  public AnalyticsDashboardDto getDashboard(int days) {
    return serviceRecordClient.getAnalyticsDashboard(days).getData();
  }
}
