package com.heloword.frontendapi.rest.analytics;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.model.dto.analytics.AnalyticsEventDto;
import com.heloword.common.model.dto.analytics.AnalyticsIngestDto;
import com.heloword.frontendapi.service.analytics.AnalyticsFrontendService;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

/**
 * Analytics. Identity is UUID-only — username/email is never read or stored.
 * Ingest ({@code /track}) is open to any logged-in visitor (member or guest) and is
 * fully fire-and-forget: it always returns success so a failure can never surface to
 * the client. Every dashboard/query endpoint is ADMIN-only.
 */
@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/analytics")
@AllArgsConstructor
public class AnalyticsRestController extends AbstractBaseFrontendRestController {

  private static final int MAX_BATCH = 50;

  private final AnalyticsFrontendService analyticsFrontendService;

  // ── Ingest (MEMBER + guest, enforced by the /fe/** filter) ──────────────────

  @PostMapping("/track")
  public HeloResponse<?> track(@RequestBody(required = false) AnalyticsIngestDto body) {
    try {
      if (body != null && body.getEvents() != null && !body.getEvents().isEmpty()) {
        Optional<UserDto> current = getUser();
        List<AnalyticsEventDto> events = body.getEvents().stream()
            .filter(e -> e != null)
            .limit(MAX_BATCH)
            .peek(e -> applyIdentity(e, current))
            .collect(Collectors.toList());
        analyticsFrontendService.ingest(events);
      }
    } catch (Exception ex) {
      // Never let analytics affect the caller.
      log.warn("analytics track failed (ignored): {}", ex.getMessage());
    }
    return success();
  }

  // ── Admin only ──────────────────────────────────────────────────────────────

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/dashboard")
  public HeloResponse<?> dashboard(@RequestParam(defaultValue = "30") int days) {
    return success(analyticsFrontendService.getDashboard(days));
  }

  /**
   * Force the identity to be UUID-only. A logged-in member's session UUID is
   * authoritative; a guest keeps their client-supplied guest UUID. Never username/email.
   */
  private void applyIdentity(AnalyticsEventDto e, Optional<UserDto> current) {
    if (current.isPresent() && current.get().getUuid() != null) {
      e.setUserUuid(current.get().getUuid());
      e.setGuest(false);
    } else {
      e.setGuest(true);
      if (e.getUserUuid() == null || e.getUserUuid().trim().isEmpty()) {
        e.setUserUuid("anonymous");
      }
    }
  }
}
