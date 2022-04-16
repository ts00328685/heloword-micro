package com.heloword.common.util;

import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import com.heloword.common.type.AuthType;

public class Util {

  public static boolean isLocalEnv(Environment environment) {
    return Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> StringUtils.contains(p, "local"));
  }

  public static String getIdTokenFromRequest(HttpServletRequest request) {
    return Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
        .filter(c -> StringUtils.equals(AuthType.GOOGLE_ID_TOKEN.getKey(), c.getName()))
        .findAny()
        .map(Cookie::getValue)
        .orElse(StringUtils.EMPTY);
  }

}
