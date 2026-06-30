package com.heloword.common.model.dto.board;

import java.util.Date;
import com.heloword.common.entity.board.LiveBoardSessionEntity;
import lombok.Data;

/**
 * Session summary. Carries only UUID-based ids + display names — no username/email.
 */
@Data
public class LiveBoardSessionDto {
  private Long id;
  private String name;
  private String boardState;
  private String createdByUserId;
  private String createdByName;
  private Date createDate;
  private Date endedDate;

  public static LiveBoardSessionDto fromEntity(LiveBoardSessionEntity e) {
    if (e == null) return null;
    LiveBoardSessionDto dto = new LiveBoardSessionDto();
    dto.setId(e.getId());
    dto.setName(e.getName());
    dto.setBoardState(e.getBoardState());
    dto.setCreatedByUserId(e.getCreatedByUserId());
    dto.setCreatedByName(e.getCreatedByName());
    dto.setCreateDate(e.getCreateDate());
    dto.setEndedDate(e.getEndedDate());
    return dto;
  }
}
