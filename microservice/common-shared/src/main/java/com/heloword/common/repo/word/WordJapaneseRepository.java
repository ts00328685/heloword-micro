package com.heloword.common.repo.word;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.word.WordJapaneseEntity;

@Repository
public interface WordJapaneseRepository extends IBaseRepo<WordJapaneseEntity, Long> {

  List<WordJapaneseEntity> findAllByInfoContaining(String info);

}
