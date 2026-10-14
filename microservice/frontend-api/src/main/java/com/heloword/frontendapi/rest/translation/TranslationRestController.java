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
import com.heloword.frontendapi.service.ai.AiGuard;
import com.heloword.frontendapi.service.ai.AiInputs;
import static com.heloword.frontendapi.service.ai.AiInputs.SENTENCE_MAX;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/translation")
@AllArgsConstructor
public class TranslationRestController extends AbstractBaseFrontendRestController {

  private TranslationAnalyzeService translationAnalyzeService;
  private AiGuard aiGuard;

  /**
   * AI analysis of user's written translation against the correct answer.
   * Open to guests; abuse is bounded by the shared daily AI quota in {@link AiGuard}.
   */
  @PreAuthorize("hasAnyAuthority(T(com.heloword.common.type.MemberRole).MEMBER.getName(), T(com.heloword.common.type.MemberRole).UNREGISTERED_MEMBER.getName())")
  @PostMapping("/ai-analyze")
  public HeloResponse<?> aiAnalyze(@RequestBody TranslationAnalyzeRequest request) {
    request.setLang(AiInputs.wordLang(request.getLang()));
    request.setTranslation(AiInputs.required(request.getTranslation(), SENTENCE_MAX));
    request.setAnswer(AiInputs.required(request.getAnswer(), SENTENCE_MAX));
    request.setUserInput(AiInputs.required(request.getUserInput(), SENTENCE_MAX));
    String result = aiGuard.run(() -> translationAnalyzeService.analyze(request));
    return HeloResponse.successWithData(result);
  }
}
