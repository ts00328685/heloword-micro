package com.heloword.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {

  UNREGISTERED_MEMBER("UNREGISTER_MEMBER"),
  MEMBER("MEMBER"),
  ADMIN("ADMIN"),
  FEIGN("FEIGN"),
  ;

  private final String name;

}
