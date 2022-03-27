package com.heloword.word.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.SentenceJapaneseEntity;
import com.heloword.common.repo.SentenceJapaneseRepository;
import com.heloword.word.service.SentenceJapaneseService;

@Service
public class SentenceJapaneseServiceImpl extends AbstractBaseServiceImpl<SentenceJapaneseEntity, Long> implements SentenceJapaneseService {

  @Autowired
  private SentenceJapaneseRepository sentenceJapaneseRepository;

  @Override
  protected SentenceJapaneseRepository getRepo() {
    return this.sentenceJapaneseRepository;
  }

}
