package com.heloword.frontendapi.service.nhkarticle;

import java.util.List;
import com.heloword.frontendapi.model.response.NhkArticleDetailDto;
import com.heloword.frontendapi.model.response.NhkArticleListItemDto;

public interface NhkArticleService {

  List<NhkArticleListItemDto> list();

  NhkArticleDetailDto getById(Long id);
}
