package com.heloword.common.repo.board;

import java.util.List;
import java.util.Optional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.board.LiveBoardSessionEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveBoardSessionRepository extends IBaseRepo<LiveBoardSessionEntity, Long> {

  Optional<LiveBoardSessionEntity> findFirstByBoardStateOrderByIdDesc(String boardState);

  List<LiveBoardSessionEntity> findAllByOrderByIdDesc();
}
