package com.heloword.frontendapi.rest.scramble;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;
import com.heloword.frontendapi.model.request.ScrambleAiRequest;
import com.heloword.frontendapi.service.scramble.ScrambleFrontendService;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/scramble")
@AllArgsConstructor
public class ScrambleRestController extends AbstractBaseFrontendRestController {

  private ScrambleFrontendService scrambleFrontendService;

  /**
   * AI grammar explanation for a sentence scramble item.
   * Restricted to MEMBER role to prevent LLM endpoint abuse by anonymous users.
   * The frontend shows the button to guests with a login prompt.
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/ai-explain")
  public HeloResponse<?> aiExplain(@RequestBody ScrambleAiRequest request) {
    String explanation = scrambleFrontendService.aiExplain(request);
    return HeloResponse.successWithData(explanation);
  }
}
