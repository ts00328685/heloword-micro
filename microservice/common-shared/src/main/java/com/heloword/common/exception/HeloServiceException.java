package com.heloword.common.exception;

import com.heloword.common.type.ResponseCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HeloServiceException extends RuntimeException {

  private ResponseCode responseCode;
  private String message;

  public static HeloServiceException of(ResponseCode responseCode, String message) {
    return HeloServiceException.builder().responseCode(responseCode).message(message).build();
  }

  public static HeloServiceException of(ResponseCode responseCode) {
    return HeloServiceException.builder().responseCode(responseCode).message(responseCode.getMessage()).build();
  }
}
