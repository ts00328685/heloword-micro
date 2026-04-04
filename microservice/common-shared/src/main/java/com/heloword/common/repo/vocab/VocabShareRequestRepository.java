package com.heloword.common.repo.vocab;

import java.util.List;
import java.util.Optional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.vocab.VocabShareRequestEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabShareRequestRepository extends IBaseRepo<VocabShareRequestEntity, Long> {

  List<VocabShareRequestEntity> findAllByToUsernameAndShareStatusAndStatus(
      String toUsername, String shareStatus, Integer status);

  Optional<VocabShareRequestEntity> findByIdAndToUsernameAndStatus(
      Long id, String toUsername, Integer status);

  Optional<VocabShareRequestEntity> findByIdAndFromUsernameAndStatus(
      Long id, String fromUsername, Integer status);
}
