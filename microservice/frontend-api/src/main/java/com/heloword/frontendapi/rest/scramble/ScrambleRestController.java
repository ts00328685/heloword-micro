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
import com.heloword.frontendapi.service.ai.AiGuard;
import com.heloword.frontendapi.service.ai.AiInputs;
import static com.heloword.frontendapi.service.ai.AiInputs.SENTENCE_MAX;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/scramble")
@AllArgsConstructor
public class ScrambleRestController extends AbstractBaseFrontendRestController {

  private ScrambleFrontendService scrambleFrontendService;
  private AiGuard aiGuard;

  /**
   * AI grammar explanation for a sentence scramble item.
   * Open to guests; abuse is bounded by the shared daily AI quota in {@link AiGuard}.
   */
  @PreAuthorize("hasAnyAuthority(T(com.heloword.common.type.MemberRole).MEMBER.getName(), T(com.heloword.common.type.MemberRole).UNREGISTERED_MEMBER.getName())")
  @PostMapping("/ai-explain")
  public HeloResponse<?> aiExplain(@RequestBody ScrambleAiRequest request) {
    request.setLang(AiInputs.wordLang(request.getLang()));
    request.setSentence(AiInputs.required(request.getSentence(), SENTENCE_MAX));
    request.setTranslation(AiInputs.optional(request.getTranslation(), SENTENCE_MAX));
    String explanation = aiGuard.run(() -> scrambleFrontendService.aiExplain(request));
    return HeloResponse.successWithData(explanation);
  }
}
