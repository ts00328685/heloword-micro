package com.heloword.common.repo.board;

import java.util.List;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.board.LiveBoardSongEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveBoardSongRepository extends IBaseRepo<LiveBoardSongEntity, Long> {

  List<LiveBoardSongEntity> findAllBySessionIdOrderBySortOrderAscIdAsc(Long sessionId);
}
