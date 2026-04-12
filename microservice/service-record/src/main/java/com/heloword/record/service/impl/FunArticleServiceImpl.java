package com.heloword.record.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.funarticle.FunArticleEntity;
import com.heloword.common.repo.funarticle.FunArticleRepository;
import com.heloword.record.service.FunArticleService;

@Service
public class FunArticleServiceImpl extends AbstractBaseServiceImpl<FunArticleEntity, Long>
    implements FunArticleService {

  @Autowired
  private FunArticleRepository funArticleRepository;

  @Override
  protected IBaseRepo<FunArticleEntity, Long> getRepo() {
    return funArticleRepository;
  }

}
