package com.heloword.common.repo;

import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.WordEnglishEntity;

@Repository
public interface WordEnglishRepository extends IBaseRepo<WordEnglishEntity, Long> {

}
