package com.heloword.common.repo.board;

import java.util.Collection;
import java.util.List;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.board.LiveBoardMessageLikeEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveBoardMessageLikeRepository extends IBaseRepo<LiveBoardMessageLikeEntity, Long> {

  boolean existsByMessageIdAndUserId(Long messageId, String userId);

  long countByMessageId(Long messageId);

  void deleteByMessageIdAndUserId(Long messageId, String userId);

  List<LiveBoardMessageLikeEntity> findAllByUserIdAndMessageIdIn(String userId, Collection<Long> messageIds);
}
