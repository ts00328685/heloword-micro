package com.heloword.common.util;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Names other people can see. A member's public name is their chosen nickname;
 * we never fall back to anything taken from their Google account (name, email),
 * so nobody's real identity is shown without them choosing it.
 */
public final class PublicNames {

  /** Unambiguous characters (no 0/O, 1/I). */
  private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final String PREFIX = "Learner-";

  private PublicNames() {}

  /** A fresh neutral nickname for a new account, e.g. "Learner-K7Q2M". */
  public static String generate() {
    StringBuilder sb = new StringBuilder(PREFIX);
    for (int i = 0; i < 5; i++) sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
    return sb.toString();
  }

  /**
   * The name to show others: the nickname if set, otherwise a stable neutral label
   * derived from the member's opaque id (same label every time, no personal data).
   */
  public static String of(String nickname, String stableId) {
    if (nickname != null && !nickname.isBlank()) return nickname;
    if (stableId == null || stableId.isBlank()) return "Learner";
    String id = stableId.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    return PREFIX + (id.length() >= 5 ? id.substring(0, 5) : id);
  }
}
