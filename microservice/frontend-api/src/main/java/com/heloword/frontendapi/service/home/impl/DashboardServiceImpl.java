package com.heloword.frontendapi.service.home.impl;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.feignclient.ServiceWordClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.type.ResponseCode;
import com.heloword.frontendapi.model.response.DashboardResponse;
import com.heloword.frontendapi.service.home.DashboardService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class DashboardServiceImpl implements DashboardService {

  @Autowired
  private ServiceWordClient serviceWordClient;

  private Callable<Integer> fromRunnable(Runnable runnable) {
    return () -> {
      runnable.run();
      return 1;
    };
  }

  @Override
  public DashboardResponse getDashboardResponse(Optional<UserDto> userDto) {
    if (userDto.isPresent()) {
      ExecutorService executorService = Executors.newFixedThreadPool(6);
      DashboardResponse dashboardResponse = new DashboardResponse();
      try {
        executorService.invokeAll(Arrays.asList(
            fromRunnable(() -> dashboardResponse.setWordEnglishList(serviceWordClient.getAllEnWords().getData())),
//            fromRunnable(() -> dashboardResponse.setWordGermanList(serviceWordClient.getAllGeWords().getData())),
//            fromRunnable(() -> dashboardResponse.setWordJapaneseList(serviceWordClient.getAllJpWords().getData())),
//            fromRunnable(() -> dashboardResponse.setSentenceEnglishList(serviceWordClient.getAllEnSentences().getData())),
//            fromRunnable(() -> dashboardResponse.setSentenceGermanList(serviceWordClient.getAllGeSentences().getData())),
            fromRunnable(() -> dashboardResponse.setSentenceJapaneseList(serviceWordClient.getAllJpSentences().getData()))
        ));
      } catch (Exception e) {
        log.error(e);
        throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
      } finally {
        executorService.shutdown();
      }
      executorService.shutdown();
      return dashboardResponse;

    } else {
      return DashboardResponse.builder().wordEnglishList(serviceWordClient.getAllEnWords().getData()).build();
    }
  }
}
