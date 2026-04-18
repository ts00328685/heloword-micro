package com.heloword.frontendapi.rest.ai;

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
import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.model.request.ai.WordCompareRequest;
import com.heloword.frontendapi.service.ai.AiFeatureService;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/ai")
@AllArgsConstructor
public class AiFeatureRestController extends AbstractBaseFrontendRestController {

  private AiFeatureService aiFeatureService;

  /**
   * AI definition + example sentence for a vocabulary word.
   * Restricted to MEMBER role — frontend shows prompt to guests.
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/word-insight")
  public HeloResponse<?> wordInsight(@RequestBody WordInsightRequest request) {
    String result = aiFeatureService.wordInsight(request);
    return HeloResponse.successWithData(result);
  }

  /**
   * A freshly generated example sentence for a vocabulary word.
   * Restricted to MEMBER role — frontend shows prompt to guests.
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/sample-sentence")
  public HeloResponse<?> sampleSentence(@RequestBody SampleSentenceRequest request) {
    String result = aiFeatureService.sampleSentence(request);
    return HeloResponse.successWithData(result);
  }

  /**
   * Personalised study tip based on the user's recent quiz performance.
   * Restricted to MEMBER role — frontend shows prompt to guests.
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/study-coach")
  public HeloResponse<?> studyCoach(@RequestBody StudyCoachRequest request) {
    String result = aiFeatureService.studyCoach(request);
    return HeloResponse.successWithData(result);
  }

  /**
   * Synonyms / similar expressions for a word with usage guidance.
   * Restricted to MEMBER role — frontend shows prompt to guests.
   */
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/word-compare")
  public HeloResponse<?> wordCompare(@RequestBody WordCompareRequest request) {
    String result = aiFeatureService.wordCompare(request);
    return HeloResponse.successWithData(result);
  }
}
