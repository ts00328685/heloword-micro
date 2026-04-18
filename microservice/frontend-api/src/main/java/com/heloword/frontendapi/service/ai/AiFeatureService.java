package com.heloword.frontendapi.service.ai;

import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.model.request.ai.WordCompareRequest;
import com.heloword.frontendapi.model.request.ai.WordFillRequest;
import com.heloword.frontendapi.model.response.WordFillResponse;

public interface AiFeatureService {
  String wordInsight(WordInsightRequest request);
  String sampleSentence(SampleSentenceRequest request);
  String studyCoach(StudyCoachRequest request);
  String wordCompare(WordCompareRequest request);
  WordFillResponse wordFill(WordFillRequest request);
}
