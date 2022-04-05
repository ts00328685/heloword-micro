package com.heloword.common.filter;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.heloword.common.type.AuthType;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

  @Value("${feign.api-key}")
  private String feignApiKey;

  @Override
  public void apply(RequestTemplate template) {
    template.headers(Map.of(
        AuthType.FEIGN_API_KEY.getKey(), List.of(feignApiKey),
        HttpHeaders.AUTHORIZATION, List.of("Bearer " + feignApiKey)
    ));
  }
}
