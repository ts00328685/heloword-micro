package com.heloword.frontendapi.rest.stats;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.frontendapi.service.stats.StatsService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/stats")
@AllArgsConstructor
public class StatsRestController extends AbstractBaseFrontendRestController {

  private StatsService statsService;

  /**
   * Request body: { "days": 7 | 30 | 0 }
   * days=0 means all-time (grouped by month).
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/daily-summary")
  public HeloResponse<?> getDailySummary(@RequestBody(required = false) Map<String, Integer> body) {
    int days = (body != null && body.containsKey("days")) ? body.get("days") : 7;
    return HeloResponse.successWithData(statsService.getDailySummary(getUser().get(), days));
  }
}
