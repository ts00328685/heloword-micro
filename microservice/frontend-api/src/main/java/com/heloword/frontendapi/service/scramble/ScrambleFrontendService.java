package com.heloword.frontendapi.service.scramble;

import com.heloword.frontendapi.model.request.ScrambleAiRequest;

public interface ScrambleFrontendService {
  String aiExplain(ScrambleAiRequest request);
}
