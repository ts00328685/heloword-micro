package com.heloword.common.repo.funarticle;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.funarticle.FunArticleEntity;

@Repository
public interface FunArticleRepository extends IBaseRepo<FunArticleEntity, Long> {
}
