package com.heloword.record.rest;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.model.dto.analytics.AnalyticsIngestDto;
import com.heloword.record.service.AnalyticsService;

/**
 * Internal analytics endpoints (reached only via the frontend-api Feign client,
 * i.e. FEIGN authority). Ingest is UUID-only; the dashboard is aggregate-only.
 */
@Log4j2
@RestController
@RequestMapping("/analytics")
public class AnalyticsRestController {

  @Autowired
  private AnalyticsService analyticsService;

  @PostMapping("/ingest")
  public HeloResponse<?> ingest(@RequestBody AnalyticsIngestDto body) {
    analyticsService.ingest(body == null ? null : body.getEvents());
    return HeloResponse.successWithoutData();
  }

  @GetMapping("/dashboard")
  public HeloResponse<?> dashboard(@RequestParam(defaultValue = "30") int days) {
    return HeloResponse.successWithData(analyticsService.getDashboard(days));
  }
}
