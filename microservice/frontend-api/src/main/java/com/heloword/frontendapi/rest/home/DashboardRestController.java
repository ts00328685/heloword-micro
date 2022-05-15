package com.heloword.frontendapi.rest.home;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.frontendapi.service.home.DashboardService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/home/dashboard")
@AllArgsConstructor
public class DashboardRestController extends AbstractBaseFrontendRestController {

  private DashboardService dashboardService;

  @PostMapping()
  public HeloResponse<?> getDashboardInfo() {
    return success(dashboardService.getDashboardResponse(getUser()));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/total-word")
  public HeloResponse<?> getTotalWord() {
    return success(dashboardService.getDashboardResponse(getUser()));
  }
}
