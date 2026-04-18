package com.heloword.frontendapi.service.translation;

import com.heloword.frontendapi.model.request.TranslationAnalyzeRequest;

public interface TranslationAnalyzeService {
  String analyze(TranslationAnalyzeRequest request);
}
