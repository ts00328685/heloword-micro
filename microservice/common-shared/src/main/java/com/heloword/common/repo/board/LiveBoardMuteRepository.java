package com.heloword.common.repo.board;

import java.util.List;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.board.LiveBoardMuteEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveBoardMuteRepository extends IBaseRepo<LiveBoardMuteEntity, Long> {

  boolean existsBySessionIdAndUserId(Long sessionId, String userId);

  List<LiveBoardMuteEntity> findAllBySessionId(Long sessionId);

  void deleteBySessionIdAndUserId(Long sessionId, String userId);
}
