package com.heloword.gateway.filter;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.feignclient.ServiceUserClient;
import com.heloword.common.util.UserSessionUtil;
import com.heloword.gateway.wrapper.CaptureRequest;
import com.heloword.gateway.wrapper.CaptureResponse;
import com.heloword.gateway.wrapper.ExchangeCaptureWrapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter {

  @Autowired
  ServiceUserClient serviceUserClient;

  @Autowired
  UserSessionUtil userSessionUtil;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

    HttpHeaders headers = exchange.getRequest().getHeaders();
    List<String> idToken = headers.getOrEmpty("idToken");
    if (!CollectionUtils.isEmpty(idToken)) {
      Optional.ofNullable(serviceUserClient.verifyGoogleId(headers.toSingleValueMap()))
          .map(HeloResponse::getData)
          .ifPresent(userSessionUtil::saveUserToSession);
    }

    ExchangeCaptureWrapper exchangeCaptureWrapper = new ExchangeCaptureWrapper(exchange);
    return chain.filter(exchangeCaptureWrapper).doOnSuccess(wrapper -> {
      CaptureRequest request = exchangeCaptureWrapper.getRequest();
      CaptureResponse response = exchangeCaptureWrapper.getResponse();
      log.debug("====> exchange Attrs {}", exchangeCaptureWrapper.getAttributes());
      log.debug("====> url {}", request.getURI());
      log.debug("====> request cookies: {}", request.getCookies());
      log.debug("====> request param: {}", request.getQueryParams());
      log.debug("====> request headers: {}", request.getHeaders());
      log.debug("====> request body: {}", request.getFullBody());
      log.debug("--------------------------------");
      log.debug("<==== response status: {}", response.getStatusCode());
      log.debug("<==== response cookies: {}", response.getCookies());
      log.debug("<==== response headers: {}", response.getHeaders());
      log.debug("<==== response body: {}", response.getFullBody());
    });
  }
}
