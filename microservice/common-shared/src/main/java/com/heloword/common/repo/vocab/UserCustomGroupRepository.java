package com.heloword.common.repo.vocab;

import java.util.List;
import java.util.Optional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.vocab.UserCustomGroupEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCustomGroupRepository extends IBaseRepo<UserCustomGroupEntity, Long> {

  List<UserCustomGroupEntity> findAllByUsernameAndStatus(String username, Integer status);

  long countByUsernameAndStatus(String username, Integer status);

  Optional<UserCustomGroupEntity> findByIdAndUsername(Long id, String username);
}
