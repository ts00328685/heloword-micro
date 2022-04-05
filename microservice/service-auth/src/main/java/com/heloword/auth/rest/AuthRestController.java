package com.heloword.auth.rest;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.heloword.auth.service.AuthService;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.MemberEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.AuthType;
import com.heloword.common.type.ResponseCode;
import com.heloword.common.util.UserSessionUtil;
import com.heloword.common.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthRestController {

  private static final String COOKIE_PATH = "/k8s";

  @Autowired
  AuthService authService;

  @Autowired
  UserSessionUtil userSessionUtil;

  @Autowired
  private Environment environment;

  @PostMapping("/verify-google-id")
  public HeloResponse<MemberEntity> verifyGoogleId(@RequestBody Map<String, ?> request, HttpServletResponse response) {
    String idToken = request.get(AuthType.GOOGLE_ID_TOKEN.getKey()).toString();

    Optional<GoogleIdToken.Payload> payload = authService.verifyGoogleIdToken(idToken);
    if (payload.isPresent()) {
      MemberEntity existingMember = authService.findOrRegisterUser(idToken, payload.get()).orElseThrow(() -> HeloServiceException.of(ResponseCode.MEMBER_FINDING_CREATION_ERROR));
      userSessionUtil.saveUserToSessionByKey(idToken, existingMember);

      Cookie cookie = new Cookie(AuthType.GOOGLE_ID_TOKEN.getKey(), idToken);
      cookie.setHttpOnly(true);
      cookie.setPath(COOKIE_PATH);
      cookie.setSecure(!Util.isLocalEnv(environment));
      response.addCookie(cookie);

      return HeloResponse.successWithData(existingMember);
    } else {
      return HeloResponse.fail(ResponseCode.GOOGLE_VERIFICATION_ERROR);
    }
  }

}
