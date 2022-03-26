package com.heloword.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

  SUCCESS("0000", "SUCCESS"),
  SYSTEM_ERROR("9999", "SYSTEM ERROR"),

  ;

  private final String code;
  private final String message;

}
