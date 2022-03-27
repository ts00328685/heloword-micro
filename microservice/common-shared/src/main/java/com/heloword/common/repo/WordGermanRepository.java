package com.heloword.common.repo;

import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.WordGermanEntity;

@Repository
public interface WordGermanRepository extends IBaseRepo<WordGermanEntity, Long> {

}
