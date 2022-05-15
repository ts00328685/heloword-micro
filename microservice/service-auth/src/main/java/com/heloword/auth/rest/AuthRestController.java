package com.heloword.auth.rest;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.heloword.auth.service.AuthService;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.type.AuthType;
import com.heloword.common.type.ResponseCode;
import com.heloword.common.util.UserSessionUtil;
import com.heloword.common.util.Util;

import lombok.extern.slf4j.Slf4j;
import static com.heloword.common.util.Util.getIdTokenFromRequest;

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
  Environment environment;

  @Value("${cipher.aes.key}")
  String aesKey;

  @Value("${cipher.aes.iv}")
  String aesIv;

  @CrossOrigin
  @PostMapping("/init-cookie")
  public HeloResponse<?> initCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(AuthType.INIT_COOKIE_KEY.getKey(), AuthType.INIT_COOKIE_VALUE.getKey());
    cookie.setHttpOnly(true);
    cookie.setPath(COOKIE_PATH);
    cookie.setSecure(!Util.isLocalEnv(environment));
    response.addCookie(cookie);
    return HeloResponse.successWithoutData();
  }

  @CrossOrigin
  @PostMapping("/init-cipher")
  public HeloResponse<?> initAesKey(HttpServletRequest request) {
    Optional.ofNullable(request.getCookies())
        .map(Arrays::stream)
        .map(cStream -> cStream.filter(c -> StringUtils.equals(AuthType.INIT_COOKIE_KEY.getKey(), c.getValue())))
        .orElseThrow(() -> HeloServiceException.of(ResponseCode.EMPTY_INIT_COOKIE));
    return HeloResponse.successWithData(Map.of(
        "aesKey", aesKey,
        "aesIv", aesIv
    ));
  }

  @PostMapping("/verify-google-id")
  public HeloResponse<UserDto> verifyGoogleId(@RequestBody Map<String, ?> request, HttpServletResponse response) {
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

      return HeloResponse.successWithData(UserDto.fromEntity(existingMember));
    } else {
      return HeloResponse.fail(ResponseCode.GOOGLE_VERIFICATION_ERROR);
    }
  }

  @PostMapping("/check-login-status")
  public HeloResponse<?> checkLoginStatus(HttpServletRequest request) {
    return Optional.ofNullable(getIdTokenFromRequest(request))
        .flatMap(userSessionUtil::getUserFromSession)
        .map(user -> Map.of("isSessionValid", true))
        .map(HeloResponse::successWithData)
        .orElseGet(HeloResponse::successWithoutData);
  }

  @PostMapping("/logout")
  public HeloResponse<?> logout(HttpServletRequest request, HttpServletResponse response) {
    String idTokenFromRequest = getIdTokenFromRequest(request);
    Cookie cookie = new Cookie(AuthType.GOOGLE_ID_TOKEN.getKey(), idTokenFromRequest);
    cookie.setPath(COOKIE_PATH);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    return Optional.ofNullable(idTokenFromRequest)
        .map(userSessionUtil::getUserFromSession)
        .map(user -> userSessionUtil.removeUserFromSessionByKey(idTokenFromRequest))
        .map(isLoggedOut -> Map.of("isLoggedOut", isLoggedOut))
        .map(HeloResponse::successWithData)
        .orElseGet(HeloResponse::successWithoutData);
  }

}
