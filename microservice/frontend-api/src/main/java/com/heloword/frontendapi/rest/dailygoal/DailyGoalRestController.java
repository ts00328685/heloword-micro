package com.heloword.frontendapi.rest.dailygoal;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.frontendapi.model.request.DailyGoalProgressRequest;
import com.heloword.frontendapi.service.dailygoal.DailyGoalFrontendService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/daily-goal")
@AllArgsConstructor
public class DailyGoalRestController extends AbstractBaseFrontendRestController {

  private DailyGoalFrontendService dailyGoalFrontendService;

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/save")
  public HeloResponse<?> save(@RequestBody DailyGoalProgressRequest request) {
    dailyGoalFrontendService.save(getUser().get(), request);
    return HeloResponse.successWithoutData();
  }
}
