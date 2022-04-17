package com.heloword.word.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.word.WordGermanEntity;
import com.heloword.common.repo.word.WordGermanRepository;
import com.heloword.word.service.WordGermanService;

@Service
public class WordGermanServiceImpl extends AbstractBaseServiceImpl<WordGermanEntity, Long> implements WordGermanService {

  @Autowired
  private WordGermanRepository wordGermanRepository;

  @Override
  protected WordGermanRepository getRepo() {
    return this.wordGermanRepository;
  }

}
