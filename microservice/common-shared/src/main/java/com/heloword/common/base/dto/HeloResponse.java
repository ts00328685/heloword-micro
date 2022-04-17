package com.heloword.common.base.dto;

import java.time.Instant;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.heloword.common.type.ResponseCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HeloResponse<T> {

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "Asia/Taipei")
  private Date timestamp;
  private String code;
  private String message;
  private T data;

  private HeloResponse(String code, String message, T data){
    this.timestamp = Date.from(Instant.now());
    this.code = code;
    this.message = message;
    this.data = data;
  }

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
