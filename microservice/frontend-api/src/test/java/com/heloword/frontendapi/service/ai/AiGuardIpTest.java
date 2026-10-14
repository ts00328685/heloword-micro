package com.heloword.frontendapi.service.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AiGuardIpTest {

  @Test
  void ipv4IsKeptAsIs() {
    assertEquals("203.0.113.7", AiGuard.normalizeIp("203.0.113.7").get());
  }

  @Test
  void takesFirstEntryOfAList() {
    assertEquals("203.0.113.7", AiGuard.normalizeIp(" 203.0.113.7, 10.0.0.1").get());
  }

  @Test
  void ipv6AddressesInTheSameSlash64ShareABucket() {
    String a = AiGuard.normalizeIp("2001:db8::1").get();
    String b = AiGuard.normalizeIp("2001:db8:0:0:ffff::2").get();
    String c = AiGuard.normalizeIp("2001:0db8:0000:0000:1234:5678:9abc:def0").get();
    assertEquals("2001:0db8:0000:0000::/64", a);
    assertEquals(a, b);
    assertEquals(a, c);
  }

  @Test
  void differentSlash64sAreDifferentBuckets() {
    assertTrue(!AiGuard.normalizeIp("2001:db8:0:1::1").get().equals(AiGuard.normalizeIp("2001:db8:0:2::1").get()));
  }

  @Test
  void rejectsNonIpValuesWithoutDnsLookup() {
    assertTrue(AiGuard.normalizeIp("example.com").isEmpty());
    assertTrue(AiGuard.normalizeIp("1.2.3.4\nFAKE LOG LINE").isEmpty());
    assertTrue(AiGuard.normalizeIp("").isEmpty());
    assertTrue(AiGuard.normalizeIp(null).isEmpty());
  }
}
