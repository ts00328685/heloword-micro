package com.heloword.frontendapi.rest.announcement;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;
import com.heloword.frontendapi.service.announcement.OfficialMessageService;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/official-messages")
@AllArgsConstructor
public class OfficialMessageRestController extends AbstractBaseFrontendRestController {

  private OfficialMessageService officialMessageService;

  @GetMapping
  public HeloResponse<?> getAll() {
    return success(officialMessageService.getAll());
  }
}
