package com.heloword.record.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.article.ArticleCrawlerEntity;
import com.heloword.common.repo.article.ArticleCrawlerRepository;
import com.heloword.record.service.ArticleCrawlerService;

@Service
public class ArticleCrawlerServiceImpl extends AbstractBaseServiceImpl<ArticleCrawlerEntity, Long>
    implements ArticleCrawlerService {

  @Autowired
  private ArticleCrawlerRepository articleCrawlerRepository;

  @Override
  protected IBaseRepo<ArticleCrawlerEntity, Long> getRepo() {
    return articleCrawlerRepository;
  }

  @Override
  public List<ArticleCrawlerEntity> listActive() {
    return articleCrawlerRepository.findByStatusOrderByCreateDateDesc(1);
  }
}
