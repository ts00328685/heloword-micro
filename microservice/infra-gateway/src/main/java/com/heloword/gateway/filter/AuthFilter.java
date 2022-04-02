package com.heloword.gateway.filter;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.feignclient.ServiceUserClient;
import com.heloword.common.type.ResponseCode;
import com.heloword.common.util.EncodeUtil;
import com.heloword.common.util.UserSessionUtil;
import com.heloword.gateway.wrapper.CaptureRequest;
import com.heloword.gateway.wrapper.CaptureResponse;
import com.heloword.gateway.wrapper.ExchangeCaptureWrapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter {

  @Value("${cipher.aes.key}")
  String AESKey;

  @Value("${cipher.aes.iv}")
  String AESIV;

  @Autowired
  ServiceUserClient serviceUserClient;

  @Autowired
  UserSessionUtil userSessionUtil;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    HttpHeaders headers = exchange.getRequest().getHeaders();
    List<String> cv = headers.getOrEmpty("cv");

    if (CollectionUtils.isEmpty(cv)) {
      throw HeloServiceException.of(ResponseCode.UNKNOWN_CLIENT);
    }

    String cvString = cv.get(0);

    if (StringUtils.isBlank(cvString)) {
      throw HeloServiceException.of(ResponseCode.INVALID_REQUEST);
    }

    EncodeUtil.verifyCV(cvString, AESKey, AESIV);

    ExchangeCaptureWrapper exchangeCaptureWrapper = new ExchangeCaptureWrapper(exchange);
    return chain.filter(exchangeCaptureWrapper).doOnSuccess(wrapper -> {
      CaptureRequest request = exchangeCaptureWrapper.getRequest();
      log.debug("<==== url {}", request.getURI());
      log.debug("<==== request cookies: {}", request.getCookies());
      log.debug("<==== request param: {}", request.getQueryParams());
      log.debug("<==== request headers: {}", request.getHeaders());
      log.debug("<==== request body: {}", request.getFullBody());
    });
  }
}
