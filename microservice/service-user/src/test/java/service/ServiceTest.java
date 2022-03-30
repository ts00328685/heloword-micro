package service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.gson.Gson;
import com.heloword.common.entity.MemberEntity;
import com.heloword.user.UserApplication;
import com.heloword.user.service.AuthService;
import com.heloword.user.service.MemberService;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
public class ServiceTest {

  @Autowired
  AuthService authService;

  @Autowired
  MemberService memberService;

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @Test
  void testAuthService() throws GeneralSecurityException, IOException {

    String idToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU4YjQyOTY2MmRiMDc4NmYyZWZlZmUxM2MxZWIxMmEyOGRjNDQyZDAiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXpwIjoiMjY4NDIxMDc0ODg1LWNuNHF0bGFzNGhlcDI1dHQ3ZjBnYWFrOHFoNTU3ZmJ1LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiYXVkIjoiMjY4NDIxMDc0ODg1LWNuNHF0bGFzNGhlcDI1dHQ3ZjBnYWFrOHFoNTU3ZmJ1LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTAwODQ4NTMxMTM2MTI2MTY3MzA4IiwiZW1haWwiOiJ0czAwMzI4Njg1QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJhdF9oYXNoIjoibDNDbGowVGFWd0E5MUp2a2lQUXgwUSIsIm5hbWUiOiJSeWFuIFRzZW5nIiwicGljdHVyZSI6Imh0dHBzOi8vbGgzLmdvb2dsZXVzZXJjb250ZW50LmNvbS9hLS9BT2gxNEdnOTJiT3JibkdkbnFrOWluWF9CSE9XTHdSaGxrYmh6QVQzMHRwd1NRPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6IlJ5YW4iLCJmYW1pbHlfbmFtZSI6IlRzZW5nIiwibG9jYWxlIjoiemgtVFciLCJpYXQiOjE2NDgzOTkyNTgsImV4cCI6MTY0ODQwMjg1OCwianRpIjoiOWQ3MDM2NDdjYmEyM2IzMzE1NDc3ZDdiZjM4ODg3NmYzNjhjMWY3OSJ9.PnalnHx0uapBvUYEMWsWTBuMkPNMv5ZOQ-tYxHV1m00l4S5Pu63IcugsAmA6cKSBA_SartOF1Ij3C2kyl34-lbWlOLh5sRKgoHqaa0xQGRWM3qF-C5sPbpVbfbYYLf8xkYmbekOKS-JtWNwj4HZndmMAjKHgsSQ2nCI0eYodW3ZnswH3goNAzdv65H-ljIs4HsAyCz08IH4pbhCCe5I-OnCWXt4ig5z9YWe9NS7W5sm4aEUgd0x0C7OnSRByeFEwKqTTp4tdDOHHfS5PzLPvV4gTx4obvGyhhBXkG-CMYYS9PWdLPJi7tB8dzEMd3oL2vNYOgPTHT5PJAytvi3p3Bg";
    Optional<GoogleIdToken.Payload> result = authService.verifyGoogleIdToken(idToken);
    result.ifPresent(payload -> authService.findOrRegisterUser(payload));

  }

  @Test
  void testMember() {

    MemberEntity member = memberService.findById(1L).orElse(null);
    Gson gson = new Gson();
    redisTemplate.opsForValue().set(member.getEmail(), gson.toJson(member));
    MemberEntity memberEntity = gson.fromJson(redisTemplate.opsForValue().get(member.getEmail()), MemberEntity.class);
    log.info("### deserialized: {} ", memberEntity);
  }

}
