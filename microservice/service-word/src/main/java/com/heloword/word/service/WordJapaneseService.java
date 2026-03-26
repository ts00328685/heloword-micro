package com.heloword.word.service;

import java.util.List;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.word.WordJapaneseEntity;

public interface WordJapaneseService extends IBaseService<WordJapaneseEntity, Long> {

  List<WordJapaneseEntity> findAllVerbs();

}
