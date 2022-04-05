package com.heloword.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthType {

  GOOGLE_ID_TOKEN("idToken", "Google id token"),
  FEIGN_API_KEY("X-FEIGN-API-KEY", "Google id token"),

  ;

  private final String key;
  private final String desc;

}
