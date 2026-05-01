package com.heloword.record.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.article.ArticleCrawlerEntity;
import com.heloword.record.service.ArticleCrawlerService;

@RestController
@RequestMapping("/article-crawler")
public class ArticleCrawlerRestController extends AbstractBaseRestController<ArticleCrawlerEntity, Long> {

  @Autowired
  private ArticleCrawlerService articleCrawlerService;

  @Override
  public IBaseService<ArticleCrawlerEntity, Long> getService() {
    return articleCrawlerService;
  }

  @GetMapping("/list")
  public HeloResponse<?> list() {
    return success(articleCrawlerService.listActive());
  }
}
