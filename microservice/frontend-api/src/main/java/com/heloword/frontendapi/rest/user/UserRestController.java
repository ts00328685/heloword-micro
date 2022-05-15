package com.heloword.frontendapi.rest.user;

import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/user")
@AllArgsConstructor
public class UserRestController extends AbstractBaseFrontendRestController {

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping()
  public HeloResponse<?> getLoggedInUser() {
    return success(Map.of("user", getUser().orElse(null)));
  }

}
