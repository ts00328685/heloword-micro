package com.heloword.common.model.dto.analytics;

import lombok.Data;

/**
 * One analytics event sent from the client. On ingest, {@code userUuid}/{@code guest}
 * are overwritten server-side for logged-in members; for guests the client-supplied
 * guest UUID is kept. Carries no username/email.
 */
@Data
public class AnalyticsEventDto {
  private String userUuid;
  private Boolean guest;
  private String sessionId;
  private String eventType;
  private String eventName;
  private String label;
  private String path;
  private String locale;
  private String device;
  private String referrer;
  private Long durationMs;
}
