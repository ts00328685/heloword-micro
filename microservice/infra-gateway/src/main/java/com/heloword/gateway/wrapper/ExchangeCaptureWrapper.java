package com.heloword.gateway.wrapper;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

public class ExchangeCaptureWrapper extends ServerWebExchangeDecorator {

  private CaptureRequest bodyCaptureRequest;
  private CaptureResponse bodyCaptureResponse;

  public ExchangeCaptureWrapper(ServerWebExchange exchange) {
    super(exchange);
    this.bodyCaptureRequest = new CaptureRequest(exchange.getRequest());
    this.bodyCaptureResponse = new CaptureResponse(exchange.getResponse());
  }

  @Override
  public CaptureRequest getRequest() {
    return bodyCaptureRequest;
  }

  @Override
  public CaptureResponse getResponse() {
    return bodyCaptureResponse;
  }

}

