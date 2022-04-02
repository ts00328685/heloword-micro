package com.heloword.gateway.config;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-1)
@RequiredArgsConstructor
@Configuration
public class GlobalExceptionConfiguration implements ErrorWebExceptionHandler {
  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    ServerHttpResponse response = exchange.getResponse();

		response.setStatusCode(HttpStatus.BAD_REQUEST);

    if (response.isCommitted()) {
      return Mono.error(ex);
    }

    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    if (ex instanceof ResponseStatusException) {
      response.setStatusCode(((ResponseStatusException) ex).getStatus());
    }

		if (ex instanceof HeloServiceException) {
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}

    return response.writeWith(Mono.fromSupplier(() -> {
          DataBufferFactory bufferFactory = response.bufferFactory();
          try {
						log.error("Gateway exception caught", ex);
            return bufferFactory.wrap(objectMapper.writeValueAsBytes(HeloResponse.fail(ResponseCode.GATEWAY_ERROR, ex.getMessage())));
          } catch (JsonProcessingException e) {
            log.error("Error writing response", ex);
            return bufferFactory.wrap(new byte[0]);
          }
        }));
  }
}
