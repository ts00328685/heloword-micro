package com.heloword.common.repo.article;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.article.ArticleCrawlerEntity;

@Repository
public interface ArticleCrawlerRepository extends IBaseRepo<ArticleCrawlerEntity, Long> {

  List<ArticleCrawlerEntity> findByStatusOrderByCreateDateDesc(Integer status);
}
