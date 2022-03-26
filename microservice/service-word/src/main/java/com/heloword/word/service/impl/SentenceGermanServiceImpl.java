package com.heloword.word.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.SentenceGermanEntity;
import com.heloword.common.repo.SentenceGermanRepository;
import com.heloword.word.service.SentenceGermanService;

@Service
public class SentenceGermanServiceImpl extends AbstractBaseServiceImpl<SentenceGermanEntity, Long> implements SentenceGermanService {

  @Autowired
  private SentenceGermanRepository sentenceGermanRepository;

  @Override
  protected SentenceGermanRepository getRepo() {
    return this.sentenceGermanRepository;
  }

}
