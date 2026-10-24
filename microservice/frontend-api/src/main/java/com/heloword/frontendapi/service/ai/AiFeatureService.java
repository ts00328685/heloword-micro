package com.heloword.frontendapi.service.ai;

import java.util.List;
import com.heloword.frontendapi.model.request.ai.AiWordRequest;
import com.heloword.frontendapi.model.request.ai.CheckSentenceRequest;
import com.heloword.frontendapi.model.request.ai.ExplainWrongRequest;
import com.heloword.frontendapi.model.request.ai.ExplainSentenceRequest;
import com.heloword.frontendapi.model.request.ai.LookupRequest;
import com.heloword.frontendapi.model.request.ai.NaturalRequest;
import com.heloword.frontendapi.model.request.ai.PolitenessRequest;
import com.heloword.frontendapi.model.response.CheckSentenceResponse;
import com.heloword.frontendapi.model.response.ClozeResponse;
import com.heloword.frontendapi.model.response.CollocationDto;
import com.heloword.frontendapi.model.response.ExplainSentenceResponse;
import com.heloword.frontendapi.model.response.LookupResponse;
import com.heloword.frontendapi.model.response.NaturalResponse;
import com.heloword.frontendapi.model.response.PolitenessResponse;
import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.model.request.ai.WordCompareRequest;
import com.heloword.frontendapi.model.request.ai.QuickTranslateRequest;
import com.heloword.frontendapi.model.request.ai.WordFillRequest;
import com.heloword.frontendapi.model.request.ai.VerbConjugationRequest;
import com.heloword.frontendapi.model.response.QuickTranslateResponse;
import com.heloword.frontendapi.model.response.WordFillResponse;
import com.heloword.frontendapi.model.response.VerbConjugationResponse;

public interface AiFeatureService {
  String wordInsight(WordInsightRequest request);
  String sampleSentence(SampleSentenceRequest request);
  String studyCoach(StudyCoachRequest request);
  String wordCompare(WordCompareRequest request);
  WordFillResponse wordFill(WordFillRequest request);
  QuickTranslateResponse quickTranslate(QuickTranslateRequest request);
  VerbConjugationResponse verbConjugation(VerbConjugationRequest request);
  CheckSentenceResponse checkSentence(CheckSentenceRequest request);
  String memoryHook(AiWordRequest request);
  String explainWrong(ExplainWrongRequest request);
  ClozeResponse cloze(AiWordRequest request);
  PolitenessResponse politeness(PolitenessRequest request);
  List<CollocationDto> collocations(AiWordRequest request);
  String wordLevel(AiWordRequest request);
  LookupResponse lookup(LookupRequest request);

  ExplainSentenceResponse explainSentence(ExplainSentenceRequest request);
  NaturalResponse natural(NaturalRequest request);
  /** @return {story, traditionalChineseTranslation} */
  String[] weeklyStory(List<String> words, String wordLang);
}
