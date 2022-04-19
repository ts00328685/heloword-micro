package com.heloword.gateway.filter;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.AuthType;
import com.heloword.common.type.ResponseCode;
import com.heloword.common.util.EncodeUtil;
import com.heloword.gateway.wrapper.CaptureRequest;
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

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    ExchangeCaptureWrapper exchangeCaptureWrapper = new ExchangeCaptureWrapper(exchange);

    String fullPath = exchange.getRequest().getPath().value();
    if (StringUtils.startsWith(fullPath, "/api/auth/init-cookie")) {
      return getVoidMono(chain, exchangeCaptureWrapper);
    }

    Optional.ofNullable(exchange.getRequest().getCookies())
        .map(cs -> cs.get(AuthType.INIT_COOKIE_KEY.getKey()))
        .filter(cookies -> cookies.stream().anyMatch(c -> StringUtils.equals(c.getValue(), AuthType.INIT_COOKIE_VALUE.getKey())))
        .orElseThrow(() -> HeloServiceException.of(ResponseCode.UNKNOWN_CLIENT));

    HttpHeaders headers = exchange.getRequest().getHeaders();
    List<String> cv = headers.getOrEmpty("cv");

    if (CollectionUtils.isEmpty(cv)) {
      log.error("cv missing");
      throw HeloServiceException.of(ResponseCode.UNKNOWN_CLIENT);
    }

    String cvString = cv.get(0);

    if (StringUtils.isBlank(cvString)) {
      log.error("cv format invalid");
      throw HeloServiceException.of(ResponseCode.INVALID_REQUEST);
    }

    EncodeUtil.verifyCV(cvString, AESKey, AESIV);

    return getVoidMono(chain, exchangeCaptureWrapper);
  }

  private Mono<Void> getVoidMono(GatewayFilterChain chain, ExchangeCaptureWrapper exchangeCaptureWrapper) {
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
