package com.heloword.frontendapi.service.home.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.feignclient.ServiceWordClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.type.ResponseCode;
import com.heloword.frontendapi.config.CacheConfig;
import com.heloword.frontendapi.model.response.DashboardResponse;
import com.heloword.frontendapi.service.home.DashboardService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class DashboardServiceImpl implements DashboardService {

  @Autowired
  private ServiceWordClient serviceWordClient;

  @Override
  // unless guard: do not cache if the primary word list is empty/null (feign failure)
  @Cacheable(value = CacheConfig.DASHBOARD_CACHE, key = "'all'",
      unless = "#result == null || #result.wordEnglishList == null || #result.wordEnglishList.isEmpty()")
  public DashboardResponse getDashboardResponse(Optional<UserDto> userDto) {
    ExecutorService pool = Executors.newFixedThreadPool(3);
    try {
      // Use CompletableFuture so exceptions from feign calls propagate instead of being swallowed
      CompletableFuture<List> enFuture  = CompletableFuture.supplyAsync(() -> serviceWordClient.getAllEnWords().getData(), pool);
      CompletableFuture<List> jpFuture  = CompletableFuture.supplyAsync(() -> serviceWordClient.getAllJpWords().getData(), pool);
      CompletableFuture<List> jpvFuture = CompletableFuture.supplyAsync(() -> serviceWordClient.getAllJpVerbWords().getData(), pool);

      return DashboardResponse.builder()
          .wordEnglishList(enFuture.get())
          .wordJapaneseList(jpFuture.get())
          .wordJapaneseVerbList(jpvFuture.get())
          .build();
    } catch (Exception e) {
      log.error("Failed to load dashboard word lists: {}", e.getMessage(), e);
      throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
    } finally {
      pool.shutdown();
    }
  }
}
