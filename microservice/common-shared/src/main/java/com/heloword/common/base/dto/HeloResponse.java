package com.heloword.common.base.dto;

import com.heloword.common.type.ResponseCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HeloResponse<T> {

  private String code;
  private String message;
  private T data;

  public static <T> HeloResponse<T> successWithoutData() {
    return new HeloResponse<T>(ResponseCode.SUCCESS.getCode(), null, null);
  }

  public static <T> HeloResponse<T> successWithData(T data) {
    return new HeloResponse<>(ResponseCode.SUCCESS.getCode(), null, data);
  }

  public static <T> HeloResponse<T> fail(ResponseCode responseCode) {
    return new HeloResponse<>(responseCode.getCode(), responseCode.getMessage(), null);
  }

  public static <T> HeloResponse<T> fail(ResponseCode responseCode, String message) {
    return new HeloResponse<>(responseCode.getCode(), message, null);
  }
}
