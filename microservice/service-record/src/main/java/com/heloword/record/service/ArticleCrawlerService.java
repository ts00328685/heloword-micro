package com.heloword.record.service;

import java.util.List;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.article.ArticleCrawlerEntity;

public interface ArticleCrawlerService extends IBaseService<ArticleCrawlerEntity, Long> {

  List<ArticleCrawlerEntity> listActive();
}
