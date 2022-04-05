package com.heloword.common.util;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

public class Util {

  public static boolean isLocalEnv(Environment environment) {
    return Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> StringUtils.contains(p, "local"));
  }

}
