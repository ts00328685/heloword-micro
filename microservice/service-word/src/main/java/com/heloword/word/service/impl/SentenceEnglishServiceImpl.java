package com.heloword.word.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.SentenceEnglishEntity;
import com.heloword.common.repo.SentenceEnglishRepository;
import com.heloword.word.service.SentenceEnglishService;

@Service
public class SentenceEnglishServiceImpl extends AbstractBaseServiceImpl<SentenceEnglishEntity, Long> implements SentenceEnglishService {

  @Autowired
  private SentenceEnglishRepository sentenceEnglishRepository;

  @Override
  protected SentenceEnglishRepository getRepo() {
    return this.sentenceEnglishRepository;
  }

}
