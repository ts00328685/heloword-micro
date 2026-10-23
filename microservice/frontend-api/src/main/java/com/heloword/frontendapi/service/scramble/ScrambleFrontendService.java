package com.heloword.frontendapi.service.scramble;

import com.heloword.frontendapi.model.request.ScrambleAiRequest;
import com.heloword.frontendapi.model.request.ScrambleHintRequest;
import com.heloword.frontendapi.model.request.ScrambleRecapRequest;
import com.heloword.frontendapi.model.request.ScrambleWrongOrderRequest;
import com.heloword.frontendapi.model.response.SimilarSentenceResponse;

public interface ScrambleFrontendService {
  String aiExplain(ScrambleAiRequest request);

  String wrongOrder(ScrambleWrongOrderRequest request);

  String hint(ScrambleHintRequest request);

  SimilarSentenceResponse similar(ScrambleAiRequest request);

  String recap(ScrambleRecapRequest request);
}
