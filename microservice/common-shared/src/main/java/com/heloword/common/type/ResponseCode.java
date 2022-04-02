package com.heloword.common.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {

  SUCCESS("0000", "SUCCESS"),
  SYSTEM_ERROR("9999", "SYSTEM ERROR"),
  UNKNOWN_CLIENT("9998", "UNKNOWN CLIENT"),
  GATEWAY_ERROR("9997", "GATEWAY ERROR"),
  INVALID_REQUEST("9996", "INVALID REQUEST"),
  GOOGLE_VERIFICATION_ERROR("9001", "GOOGLE VERIFICATION ERROR"),
  MEMBER_FINDING_CREATION_ERROR("9002", "MEMBER FINDING OR CREATION ERROR"),

  ;

  private final String code;
  private final String message;

}
