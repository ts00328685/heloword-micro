package com.heloword.frontendapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Daily AI usage limits. All counters reset at midnight Asia/Taipei. */
@Data
@Component
@ConfigurationProperties(prefix = "helo.ai.quota")
public class AiQuotaProperties {

  /** AI calls per guest (signed cookie) per day, shared across all AI features. */
  private int guestDaily = 10;

  /** AI calls per client IP per day across all guests on that IP (stops cookie-clearing farms). */
  private int ipDaily = 15;

  /** AI calls per day across ALL guests combined — hard ceiling protecting the GPU. */
  private int guestGlobalDaily = 500;

  /** AI calls per member per day (accounts are free, so members need a ceiling too). */
  private int memberDaily = 200;

  /** Max guest LLM calls in flight at once per pod, so guests can never starve members. */
  private int guestConcurrency = 2;

  /** Seconds a guest request waits for a free slot before getting AI_BUSY. */
  private int guestWaitSeconds = 10;

  /** Max guest requests queued for a slot per pod; beyond this they get AI_BUSY immediately. */
  private int guestMaxWaiting = 4;

  /** Max LLM calls in flight at once per member account. */
  private int memberConcurrency = 2;

  /** HMAC secret for signing guest cookies. Must be identical on every pod. */
  private String guestSecret;

  /** Header carrying the real client IP (set by nginx ingress; use CF-Connecting-IP behind Cloudflare). */
  private String clientIpHeader = "X-Real-IP";

  /** Secure flag on the guest cookie; disable only for plain-http local dev. */
  private boolean cookieSecure = true;
}
