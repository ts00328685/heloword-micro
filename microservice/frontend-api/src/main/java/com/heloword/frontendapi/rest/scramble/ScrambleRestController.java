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
import java.util.List;
import java.util.stream.Collectors;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;
import com.heloword.frontendapi.model.request.ScrambleAiRequest;
import com.heloword.frontendapi.model.request.ScrambleHintRequest;
import com.heloword.frontendapi.model.request.ScrambleRecapRequest;
import com.heloword.frontendapi.model.request.ScrambleWrongOrderRequest;
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

  private static final String ANY_MEMBER =
      "hasAnyAuthority(T(com.heloword.common.type.MemberRole).MEMBER.getName(), T(com.heloword.common.type.MemberRole).UNREGISTERED_MEMBER.getName())";

  /** Explain what is wrong with a fully placed but misordered answer. */
  @PreAuthorize(ANY_MEMBER)
  @PostMapping("/ai-wrong-order")
  public HeloResponse<?> aiWrongOrder(@RequestBody ScrambleWrongOrderRequest request) {
    request.setLang(AiInputs.wordLang(request.getLang()));
    request.setSentence(AiInputs.required(request.getSentence(), SENTENCE_MAX));
    request.setAttempt(AiInputs.required(request.getAttempt(), SENTENCE_MAX + 60));
    request.setTranslation(AiInputs.optional(request.getTranslation(), SENTENCE_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> scrambleFrontendService.wrongOrder(request)));
  }

  /** A one-line grammar nudge toward the next chunk. */
  @PreAuthorize(ANY_MEMBER)
  @PostMapping("/ai-hint")
  public HeloResponse<?> aiHint(@RequestBody ScrambleHintRequest request) {
    request.setLang(AiInputs.wordLang(request.getLang()));
    request.setSentence(AiInputs.required(request.getSentence(), SENTENCE_MAX));
    request.setPlaced(AiInputs.optional(request.getPlaced(), SENTENCE_MAX + 60));
    request.setTranslation(AiInputs.optional(request.getTranslation(), SENTENCE_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> scrambleFrontendService.hint(request)));
  }

  /** A new playable sentence with the same grammar pattern. */
  @PreAuthorize(ANY_MEMBER)
  @PostMapping("/ai-similar")
  public HeloResponse<?> aiSimilar(@RequestBody ScrambleAiRequest request) {
    request.setLang(AiInputs.wordLang(request.getLang()));
    request.setSentence(AiInputs.required(request.getSentence(), SENTENCE_MAX));
    request.setTranslation(AiInputs.optional(request.getTranslation(), SENTENCE_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> scrambleFrontendService.similar(request)));
  }

  /** Up to 3 grammar points behind today's tricky sentences (max 10 sentences). */
  @PreAuthorize(ANY_MEMBER)
  @PostMapping("/ai-recap")
  public HeloResponse<?> aiRecap(@RequestBody ScrambleRecapRequest request) {
    request.setLang(AiInputs.wordLang(request.getLang()));
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw HeloServiceException.of(ResponseCode.INVALID_REQUEST);
    }
    List<ScrambleRecapRequest.Item> items = request.getItems().stream().limit(10).map(it -> {
      it.setSentence(AiInputs.required(it.getSentence(), SENTENCE_MAX));
      it.setTranslation(AiInputs.optional(it.getTranslation(), SENTENCE_MAX));
      return it;
    }).collect(Collectors.toList());
    request.setItems(items);
    return HeloResponse.successWithData(aiGuard.run(() -> scrambleFrontendService.recap(request)));
  }
}
