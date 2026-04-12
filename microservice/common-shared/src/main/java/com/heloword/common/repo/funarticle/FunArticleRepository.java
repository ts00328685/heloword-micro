package com.heloword.common.repo.funarticle;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.funarticle.FunArticleEntity;

@Repository
public interface FunArticleRepository extends IBaseRepo<FunArticleEntity, Long> {

  @Query(value = "SELECT * FROM FUN_ARTICLE ORDER BY RAND() LIMIT 5", nativeQuery = true)
  List<FunArticleEntity> findRandom5();
}
