package com.heloword.frontendapi.service.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.net.InetAddress;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.filter.ServiceAuthFilter;
import com.heloword.common.type.ResponseCode;
import com.heloword.frontendapi.config.AiQuotaProperties;

/**
 * Gatekeeper for every LLM call.
 *
 * <p>Members: one daily counter per username.
 * Guests: identified by a server-signed HttpOnly cookie, and limited by three counters at once —
 * per guest, per client IP (so clearing cookies doesn't reset anything), and a global guest ceiling.
 * All counters are checked and incremented atomically in Redis, so concurrent requests cannot
 * slip past the limit and the limits hold across pods and restarts.
 *
 * <p>Quota is only refunded when the call is rejected before reaching the LLM (AI_BUSY).
 * Refunding on later failures would let anyone get free GPU time with inputs that make the
 * model's output unparseable (e.g. non-verbs for verb conjugation).
 */
@Log4j2
@Component
public class AiGuard {

  static final String GUEST_COOKIE = "hw_gid";
  private static final ZoneId ZONE = ZoneId.of("Asia/Taipei");
  private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;
  private static final long COUNTER_TTL_SECONDS = Duration.ofHours(26).getSeconds();

  /**
   * KEYS = counters, ARGV[1] = TTL, ARGV[i+1] = limit for KEYS[i].
   * Returns 0 and increments all counters if every one is under its limit,
   * otherwise returns the 1-based index of the first exhausted counter and changes nothing.
   */
  private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>(
      "for i, k in ipairs(KEYS) do\n"
      + "  if tonumber(redis.call('GET', k) or '0') >= tonumber(ARGV[i + 1]) then return i end\n"
      + "end\n"
      + "for i, k in ipairs(KEYS) do\n"
      + "  redis.call('INCR', k)\n"
      + "  redis.call('EXPIRE', k, ARGV[1])\n"
      + "end\n"
      + "return 0", Long.class);

  @Autowired
  private AiQuotaProperties props;

  @Autowired
  private StringRedisTemplate redis;

  private Semaphore guestSlots;
  private final AtomicInteger guestWaiting = new AtomicInteger();
  private byte[] secret;

  @PostConstruct
  void init() {
    guestSlots = new Semaphore(Math.max(1, props.getGuestConcurrency()), true);
    if (StringUtils.isBlank(props.getGuestSecret())) {
      // Still safe (IP + global caps apply), but cookies won't validate across pods/restarts.
      log.warn("helo.ai.quota.guest-secret is not set — using a random per-pod secret");
      byte[] random = new byte[32];
      new SecureRandom().nextBytes(random);
      secret = random;
    } else {
      secret = props.getGuestSecret().getBytes(StandardCharsets.UTF_8);
    }
  }

  /** Runs an LLM call after charging one unit of the caller's daily quota. */
  public <T> T run(Supplier<T> llmCall) {
    Caller caller = resolveCaller(true);
    consume(caller);
    return caller.isGuest() ? runAsGuest(caller, llmCall) : runAsMember(caller, llmCall);
  }

  /**
   * Guests share a few LLM slots. Waiters are capped too: each waiting request holds a servlet
   * thread, so an unbounded queue would let a burst of guest requests exhaust the thread pool
   * and take down every endpoint, not just AI.
   */
  private <T> T runAsGuest(Caller caller, Supplier<T> llmCall) {
    if (guestWaiting.incrementAndGet() > props.getGuestMaxWaiting()) {
      guestWaiting.decrementAndGet();
      refund(caller);
      throw HeloServiceException.of(ResponseCode.AI_BUSY);
    }
    boolean acquired = false;
    try {
      acquired = guestSlots.tryAcquire(props.getGuestWaitSeconds(), TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      guestWaiting.decrementAndGet();
    }
    if (!acquired) {
      refund(caller);
      throw HeloServiceException.of(ResponseCode.AI_BUSY);
    }
    try {
      return llmCall.get();
    } finally {
      guestSlots.release();
    }
  }

  /** Members get a per-account in-flight cap (shared across pods via Redis) so one script can't flood the GPU. */
  private <T> T runAsMember(Caller caller, Supplier<T> llmCall) {
    String key = "ai:inflight:" + caller.getLogId();
    Long inFlight = redis.opsForValue().increment(key);
    // TTL only matters if a pod dies mid-call; it must outlive the LLM read timeout.
    redis.expire(key, 120, TimeUnit.SECONDS);
    if (inFlight != null && inFlight > props.getMemberConcurrency()) {
      redis.opsForValue().decrement(key);
      refund(caller);
      throw HeloServiceException.of(ResponseCode.AI_BUSY);
    }
    try {
      return llmCall.get();
    } finally {
      redis.opsForValue().decrement(key);
    }
  }

  /** Remaining calls today for the current caller, without consuming anything. */
  public QuotaStatus status() {
    Caller caller = resolveCaller(false);
    List<String> keys = caller.getKeys();
    List<Integer> limits = caller.getLimits();
    List<String> values = redis.opsForValue().multiGet(keys);
    int remaining = Integer.MAX_VALUE;
    for (int i = 0; i < keys.size(); i++) {
      String v = values == null ? null : values.get(i);
      int used = v == null ? 0 : Integer.parseInt(v);
      remaining = Math.min(remaining, Math.max(0, limits.get(i) - used));
    }
    return new QuotaStatus(caller.isGuest(), limits.get(0), remaining);
  }

  private void consume(Caller caller) {
    List<String> argv = new ArrayList<>();
    argv.add(String.valueOf(COUNTER_TTL_SECONDS));
    caller.getLimits().forEach(l -> argv.add(String.valueOf(l)));

    Long exhausted = redis.execute(CONSUME_SCRIPT, caller.getKeys(), argv.toArray());
    if (exhausted != null && exhausted != 0L) {
      log.info("AI quota exhausted: caller={} counter={}", caller.getLogId(), caller.getKeys().get(exhausted.intValue() - 1));
      throw HeloServiceException.of(ResponseCode.AI_QUOTA_EXCEEDED);
    }
  }

  private void refund(Caller caller) {
    caller.getKeys().forEach(k -> redis.opsForValue().decrement(k));
  }

  // ── Caller resolution ──────────────────────────────────────────────────────

  private Caller resolveCaller(boolean issueCookieIfMissing) {
    // Hash tag {day} keeps all of a day's keys in one slot, so the Lua script also works on Redis Cluster.
    String prefix = "ai:{" + LocalDate.now(ZONE).format(DAY) + "}:";

    Optional<String> username = currentUsername();
    if (username.isPresent()) {
      return new Caller(false, "member:" + username.get(),
          List.of(prefix + "m:" + username.get()),
          List.of(props.getMemberDaily()));
    }

    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpServletRequest request = attrs.getRequest();
    HttpServletResponse response = attrs.getResponse();

    String guestId = readGuestCookie(request).orElse(null);
    if (guestId == null) {
      guestId = UUID.randomUUID().toString();
      if (issueCookieIfMissing && response != null) writeGuestCookie(response, guestId);
    }
    String ip = clientIp(request);

    return new Caller(true, "guest:" + guestId + "@" + ip,
        List.of(prefix + "g:" + guestId, prefix + "ip:" + ip, prefix + "guests"),
        List.of(props.getGuestDaily(), props.getIpDaily(), props.getGuestGlobalDaily()));
  }

  private Optional<String> currentUsername() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getDetails() instanceof ServiceAuthFilter.CustomUserDetails)) return Optional.empty();
    MemberEntity member = ((ServiceAuthFilter.CustomUserDetails) auth.getDetails()).getMemberEntity();
    return Optional.ofNullable(member).map(MemberEntity::getUsername).filter(StringUtils::isNotBlank);
  }

  /**
   * Client IP from the ingress-set header, falling back to the socket address.
   * IPv6 is bucketed to its /64 (parsed, not string-split, so compressed forms like
   * 2001:db8::1 and 2001:db8::2 land in the same bucket) so rotating addresses within one
   * allocation doesn't reset the limit. Anything that isn't an IP literal is ignored.
   */
  private String clientIp(HttpServletRequest request) {
    return normalizeIp(request.getHeader(props.getClientIpHeader()))
        .or(() -> normalizeIp(request.getRemoteAddr()))
        .orElse("unknown");
  }

  static Optional<String> normalizeIp(String raw) {
    String ip = StringUtils.substringBefore(StringUtils.trimToEmpty(raw), ",").trim();
    // Only hand IP literals to InetAddress — a hostname would trigger a DNS lookup.
    if (ip.isEmpty() || ip.length() > 45 || !IP_LITERAL.matcher(ip).matches()) return Optional.empty();
    try {
      byte[] addr = InetAddress.getByName(ip).getAddress();
      if (addr.length == 4) return Optional.of(InetAddress.getByAddress(addr).getHostAddress());
      StringBuilder prefix = new StringBuilder();
      for (int i = 0; i < 8; i += 2) {
        prefix.append(String.format("%02x%02x", addr[i], addr[i + 1])).append(i < 6 ? ":" : "::/64");
      }
      return Optional.of(prefix.toString());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private static final Pattern IP_LITERAL = Pattern.compile("[0-9a-fA-F:.]+");

  // ── Signed guest cookie: "<uuid>.<base64url HMAC-SHA256(uuid)>" ─────────────

  private Optional<String> readGuestCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) return Optional.empty();
    for (Cookie c : cookies) {
      if (!GUEST_COOKIE.equals(c.getName()) || c.getValue() == null || c.getValue().length() > 128) continue;
      String id = StringUtils.substringBefore(c.getValue(), ".");
      String sig = StringUtils.substringAfter(c.getValue(), ".");
      if (MessageDigest.isEqual(sign(id).getBytes(StandardCharsets.US_ASCII), sig.getBytes(StandardCharsets.US_ASCII))) {
        return Optional.of(id);
      }
    }
    return Optional.empty();
  }

  private void writeGuestCookie(HttpServletResponse response, String guestId) {
    ResponseCookie cookie = ResponseCookie.from(GUEST_COOKIE, guestId + "." + sign(guestId))
        .httpOnly(true)
        .secure(props.isCookieSecure())
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.ofDays(365))
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("HMAC unavailable", e);
    }
  }

  @Data
  @AllArgsConstructor
  private static class Caller {
    private boolean guest;
    private String logId;
    private List<String> keys;
    /** Aligned with keys; index 0 is the caller's own counter. */
    private List<Integer> limits;
  }

  @Data
  @AllArgsConstructor
  public static class QuotaStatus {
    private boolean guest;
    private int limit;
    private int remaining;
  }
}
