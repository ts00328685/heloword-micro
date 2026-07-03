package com.heloword.common.entity.analytics;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A single analytics event (page view / button / feature usage). Identity is
 * UUID-only — a member UUID or a guest's local UUID — never username/email or any
 * other PII. {@code createDate} (from {@link BaseEntity}) is the server-side event time.
 */
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "analytics_event_seq")
@Table(name = "ANALYTICS_EVENT")
public class AnalyticsEventEntity extends BaseEntity {

  /** Member UUID, or the guest's local UUID. Never username/email. */
  private String userUuid;

  /** True when the actor is a not-logged-in guest. */
  private Boolean guest;

  /** Per-visit client session id (ephemeral, client-generated). */
  private String sessionId;

  /** PAGE_VIEW | BUTTON | FEATURE. */
  private String eventType;

  /** Route path (e.g. {@code /vocabulary}) or action key (e.g. {@code quiz.start}). */
  private String eventName;

  /**
   * Human-readable subject of the event — an article title, a word, a button's derived
   * label, etc. Nullable; used for content-level ("what was viewed") granularity.
   */
  @Column(length = 512)
  private String label;

  /** The route the user was on when the event fired. */
  @Column(length = 512)
  private String path;

  /** UI locale (e.g. {@code en}, {@code ja}). */
  private String locale;

  /** {@code mobile} | {@code desktop} (derived from viewport width). */
  private String device;

  /** First-visit referrer, if any. */
  @Column(length = 512)
  private String referrer;

  /** Page dwell time in milliseconds (measured on page-view exit), if available. */
  private Long durationMs;
}
