package com.heloword.common.repo.vocab;

import java.util.List;
import java.util.Optional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.vocab.SharedVocabGroupEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface SharedVocabGroupRepository extends IBaseRepo<SharedVocabGroupEntity, Long> {

  List<SharedVocabGroupEntity> findAllByShareStatusAndStatus(String shareStatus, Integer status);

  Optional<SharedVocabGroupEntity> findByRequesterUsernameAndGroupIdAndStatus(
      String requesterUsername, Long groupId, Integer status);

  Optional<SharedVocabGroupEntity> findByIdAndStatus(Long id, Integer status);
}
