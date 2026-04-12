package com.heloword.frontendapi.service.funarticle;

import java.util.List;
import com.heloword.frontendapi.model.response.FunArticleDto;

public interface FunArticleService {
  FunArticleDto getLatest();
  List<FunArticleDto> getAll();
}
