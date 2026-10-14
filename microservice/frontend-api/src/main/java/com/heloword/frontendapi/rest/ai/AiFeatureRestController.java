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
import static com.heloword.frontendapi.service.ai.AiInputs.MEANING_MAX;
import static com.heloword.frontendapi.service.ai.AiInputs.SENTENCE_MAX;
import static com.heloword.frontendapi.service.ai.AiInputs.WORD_MAX;
import com.heloword.frontendapi.model.request.ai.AiWordRequest;
import com.heloword.frontendapi.model.request.ai.CheckSentenceRequest;
import com.heloword.frontendapi.model.request.ai.ExplainWrongRequest;
import com.heloword.frontendapi.model.request.ai.PolitenessRequest;
import com.heloword.frontendapi.model.request.ai.QuickTranslateRequest;
import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.model.request.ai.WordCompareRequest;
import com.heloword.frontendapi.model.request.ai.WordFillRequest;
import com.heloword.frontendapi.model.request.ai.VerbConjugationRequest;
import com.heloword.frontendapi.service.ai.AiFeatureService;
import com.heloword.frontendapi.service.ai.AiGuard;
import com.heloword.frontendapi.service.ai.AiInputs;
import com.heloword.frontendapi.service.ai.WeeklyStoryService;

/**
 * AI features. Every LLM call goes through {@link AiGuard}: guests share a small daily quota
 * (per signed cookie + per IP + global ceiling), members have a larger per-account quota.
 * Inputs are trimmed and length-capped before they reach any prompt or cache key.
 */
@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/ai")
@AllArgsConstructor
public class AiFeatureRestController extends AbstractBaseFrontendRestController {

  /** Members and guests. Resolved from the enum: the guest authority string is "UNREGISTER_MEMBER". */
  private static final String ANYONE = "hasAnyAuthority(T(com.heloword.common.type.MemberRole).MEMBER.getName(), T(com.heloword.common.type.MemberRole).UNREGISTERED_MEMBER.getName())";
  private static final String MEMBER = "hasAnyAuthority('MEMBER')";

  private AiFeatureService aiFeatureService;
  private WeeklyStoryService weeklyStoryService;
  private AiGuard aiGuard;

  /** Remaining AI calls today for the caller. Never consumes quota. */
  @PreAuthorize(ANYONE)
  @PostMapping("/quota")
  public HeloResponse<?> quota() {
    return HeloResponse.successWithData(aiGuard.status());
  }

  /** AI definition + example sentence for a vocabulary word. */
  @PreAuthorize(ANYONE)
  @PostMapping("/word-insight")
  public HeloResponse<?> wordInsight(@RequestBody WordInsightRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    request.setWordLang(AiInputs.wordLang(request.getWordLang(), request.getWord()));
    request.setTranslateEn(AiInputs.optional(request.getTranslateEn(), MEANING_MAX));
    request.setTranslateCh(AiInputs.optional(request.getTranslateCh(), MEANING_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.wordInsight(request)));
  }

  /** A freshly generated example sentence for a vocabulary word. */
  @PreAuthorize(ANYONE)
  @PostMapping("/sample-sentence")
  public HeloResponse<?> sampleSentence(@RequestBody SampleSentenceRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    request.setWordLang(AiInputs.wordLang(request.getWordLang(), request.getWord()));
    request.setTranslateEn(AiInputs.optional(request.getTranslateEn(), MEANING_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.sampleSentence(request)));
  }

  /** Personalised study tip based on the member's recent quiz performance. */
  @PreAuthorize(MEMBER)
  @PostMapping("/study-coach")
  public HeloResponse<?> studyCoach(@RequestBody StudyCoachRequest request) {
    request.setAccuracyPct(Math.max(0, Math.min(100, request.getAccuracyPct())));
    request.setWrongCount(Math.max(0, Math.min(9999, request.getWrongCount())));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.studyCoach(request)));
  }

  /** Synonyms / similar expressions for a word with usage guidance. */
  @PreAuthorize(ANYONE)
  @PostMapping("/word-compare")
  public HeloResponse<?> wordCompare(@RequestBody WordCompareRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    request.setWordLang(AiInputs.wordLang(request.getWordLang(), request.getWord()));
    request.setTranslateEn(AiInputs.optional(request.getTranslateEn(), MEANING_MAX));
    request.setTranslateCh(AiInputs.optional(request.getTranslateCh(), MEANING_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.wordCompare(request)));
  }

  /** AI fill for the custom vocab form — meanings, example sentence and level tag in one call. */
  @PreAuthorize(MEMBER)
  @PostMapping("/word-fill")
  public HeloResponse<?> wordFill(@RequestBody WordFillRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    request.setWordLang(AiInputs.wordLang(request.getWordLang(), request.getWord()));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.wordFill(request)));
  }

  /** Auto-detects language and returns a quick translation. */
  @PreAuthorize(ANYONE)
  @PostMapping("/quick-translate")
  public HeloResponse<?> quickTranslate(@RequestBody QuickTranslateRequest request) {
    request.setText(AiInputs.required(request.getText(), WORD_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.quickTranslate(request)));
  }

  /** Verb conjugation table for English or Japanese verbs; error if the input is not a verb. */
  @PreAuthorize(ANYONE)
  @PostMapping("/verb-conjugation")
  public HeloResponse<?> verbConjugation(@RequestBody VerbConjugationRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.verbConjugation(request)));
  }

  /** Checks a sentence the learner wrote with the word; returns a fix when it's wrong. */
  @PreAuthorize(ANYONE)
  @PostMapping("/check-sentence")
  public HeloResponse<?> checkSentence(@RequestBody CheckSentenceRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    request.setWordLang(AiInputs.wordLang(request.getWordLang(), request.getWord()));
    request.setSentence(AiInputs.required(request.getSentence(), SENTENCE_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.checkSentence(request)));
  }

  /** One-line mnemonic for remembering the word. */
  @PreAuthorize(ANYONE)
  @PostMapping("/memory-hook")
  public HeloResponse<?> memoryHook(@RequestBody AiWordRequest request) {
    normalize(request);
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.memoryHook(request)));
  }

  /** Why the picked multiple-choice answer is wrong. */
  @PreAuthorize(ANYONE)
  @PostMapping("/explain-wrong")
  public HeloResponse<?> explainWrong(@RequestBody ExplainWrongRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    request.setWordLang(AiInputs.wordLang(request.getWordLang(), request.getWord()));
    request.setPicked(AiInputs.required(request.getPicked(), MEANING_MAX));
    request.setAnswer(AiInputs.required(request.getAnswer(), MEANING_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.explainWrong(request)));
  }

  /** Fill-in-the-blank sentence for the word. */
  @PreAuthorize(ANYONE)
  @PostMapping("/cloze")
  public HeloResponse<?> cloze(@RequestBody AiWordRequest request) {
    normalize(request);
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.cloze(request)));
  }

  /** Rewrites a Japanese sentence as casual / polite / keigo. Non-Japanese input is rejected for free. */
  @PreAuthorize(ANYONE)
  @PostMapping("/politeness")
  public HeloResponse<?> politeness(@RequestBody PolitenessRequest request) {
    request.setSentence(AiInputs.japanese(request.getSentence(), SENTENCE_MAX));
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.politeness(request)));
  }

  /** Three common word pairings with Traditional Chinese meanings. */
  @PreAuthorize(ANYONE)
  @PostMapping("/collocations")
  public HeloResponse<?> collocations(@RequestBody AiWordRequest request) {
    normalize(request);
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.collocations(request)));
  }

  /** JLPT / CEFR level tag for a saved custom word. */
  @PreAuthorize(MEMBER)
  @PostMapping("/word-level")
  public HeloResponse<?> wordLevel(@RequestBody AiWordRequest request) {
    normalize(request);
    return HeloResponse.successWithData(aiGuard.run(() -> aiFeatureService.wordLevel(request)));
  }

  /** This week's story from the member's most-missed words; generated once per week. */
  @PreAuthorize(MEMBER)
  @PostMapping("/weekly-story")
  public HeloResponse<?> weeklyStory() {
    return HeloResponse.successWithData(weeklyStoryService.get(getUser().get()));
  }

  private void normalize(AiWordRequest request) {
    request.setWord(AiInputs.required(request.getWord(), WORD_MAX));
    request.setWordLang(AiInputs.wordLang(request.getWordLang(), request.getWord()));
    request.setMeaningZh(AiInputs.optional(request.getMeaningZh(), MEANING_MAX));
  }
}
