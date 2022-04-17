package com.heloword.word.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.word.WordJapaneseEntity;
import com.heloword.common.repo.word.WordJapaneseRepository;
import com.heloword.word.service.WordJapaneseService;

@Service
public class WordJapaneseServiceImpl extends AbstractBaseServiceImpl<WordJapaneseEntity, Long> implements WordJapaneseService {

  @Autowired
  private WordJapaneseRepository wordJapaneseRepository;

  @Override
  protected WordJapaneseRepository getRepo() {
    return this.wordJapaneseRepository;
  }

}
