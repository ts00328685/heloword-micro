package com.heloword.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthType {

  GOOGLE_ID_TOKEN("idToken", "Google id token"),
  FEIGN_API_KEY("X-FEIGN-API-KEY", "Google id token"),
  INIT_COOKIE_KEY("INIT_COOKIE_KEY", "Init cookie key"),
  INIT_COOKIE_VALUE("INIT_COOKIE_VALUE", "Init cookie value"),

  ;

  private final String key;
  private final String desc;

}
