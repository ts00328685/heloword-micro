package com.heloword.frontendapi.rest.translation;

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
import com.heloword.frontendapi.model.request.TranslationAnalyzeRequest;
import com.heloword.frontendapi.service.translation.TranslationAnalyzeService;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/translation")
@AllArgsConstructor
public class TranslationRestController extends AbstractBaseFrontendRestController {

  private TranslationAnalyzeService translationAnalyzeService;

  /**
   * AI analysis of user's written translation against the correct answer.
   * Restricted to MEMBER role to prevent LLM endpoint abuse by anonymous users.
   * The frontend shows the button to guests with a login prompt.
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/ai-analyze")
  public HeloResponse<?> aiAnalyze(@RequestBody TranslationAnalyzeRequest request) {
    String result = translationAnalyzeService.analyze(request);
    return HeloResponse.successWithData(result);
  }
}
