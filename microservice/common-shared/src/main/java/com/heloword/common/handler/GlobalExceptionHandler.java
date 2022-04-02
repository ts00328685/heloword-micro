package com.heloword.common.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = {Exception.class})
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  public HeloResponse<?> handleExceptions(Exception ex, WebRequest request) {

    if (ex instanceof HeloServiceException) {
      log.error("handling global service error", ex);
      return HeloResponse.fail(((HeloServiceException) ex).getResponseCode());
    }

    log.error("handling uncaught global error", ex);
    return HeloResponse.fail(ResponseCode.SYSTEM_ERROR);
  }

}
