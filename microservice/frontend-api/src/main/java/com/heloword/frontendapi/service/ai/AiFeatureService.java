package com.heloword.frontendapi.service.ai;

import com.heloword.frontendapi.model.request.ai.SampleSentenceRequest;
import com.heloword.frontendapi.model.request.ai.StudyCoachRequest;
import com.heloword.frontendapi.model.request.ai.WordInsightRequest;
import com.heloword.frontendapi.model.request.ai.WordCompareRequest;

public interface AiFeatureService {
  String wordInsight(WordInsightRequest request);
  String sampleSentence(SampleSentenceRequest request);
  String studyCoach(StudyCoachRequest request);
  String wordCompare(WordCompareRequest request);
}
