package com.heloword.common.model.dto.board;

import com.heloword.common.entity.board.LiveBoardSongEntity;
import lombok.Data;

@Data
public class LiveBoardSongDto {
  private Long id;
  private Long sessionId;
  private String title;
  private Boolean sung;
  private Boolean performing;
  private Integer requestCount;
  private Integer sortOrder;

  public static LiveBoardSongDto fromEntity(LiveBoardSongEntity e) {
    if (e == null) return null;
    LiveBoardSongDto dto = new LiveBoardSongDto();
    dto.setId(e.getId());
    dto.setSessionId(e.getSessionId());
    dto.setTitle(e.getTitle());
    dto.setSung(Boolean.TRUE.equals(e.getSung()));
    dto.setPerforming(Boolean.TRUE.equals(e.getPerforming()));
    dto.setRequestCount(e.getRequestCount() == null ? 0 : e.getRequestCount());
    dto.setSortOrder(e.getSortOrder() == null ? 0 : e.getSortOrder());
    return dto;
  }
}
