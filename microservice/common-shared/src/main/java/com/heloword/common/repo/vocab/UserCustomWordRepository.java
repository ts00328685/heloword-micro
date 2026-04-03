package com.heloword.common.repo.vocab;

import java.util.List;
import java.util.Optional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.vocab.UserCustomWordEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCustomWordRepository extends IBaseRepo<UserCustomWordEntity, Long> {

  List<UserCustomWordEntity> findAllByGroupIdAndStatus(Long groupId, Integer status);

  Optional<UserCustomWordEntity> findByIdAndUsername(Long id, String username);

  long countByGroupIdAndStatus(Long groupId, Integer status);

  void deleteAllByGroupId(Long groupId);
}
