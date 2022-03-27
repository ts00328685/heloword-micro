package com.heloword.common.repo;

import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.WordJapaneseEntity;

@Repository
public interface WordJapaneseRepository extends IBaseRepo<WordJapaneseEntity, Long> {

}