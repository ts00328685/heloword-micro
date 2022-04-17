package com.heloword.word.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.word.WordEnglishEntity;
import com.heloword.common.repo.word.WordEnglishRepository;
import com.heloword.word.service.WordEnglishService;

@Service
public class WordEnglishServiceImpl extends AbstractBaseServiceImpl<WordEnglishEntity, Long> implements WordEnglishService {

  @Autowired
  private WordEnglishRepository wordEnglishRepository;

  @Override
  protected WordEnglishRepository getRepo() {
    return this.wordEnglishRepository;
  }

}
