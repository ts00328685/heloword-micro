package com.heloword.common.repo.board;

import java.util.List;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.board.LiveBoardMessageEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveBoardMessageRepository extends IBaseRepo<LiveBoardMessageEntity, Long> {

  List<LiveBoardMessageEntity> findAllBySessionIdAndDeletedFalseOrderByIdAsc(Long sessionId);
}
