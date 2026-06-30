package com.heloword.common.model.dto.board;

import java.util.Date;
import com.heloword.common.entity.board.LiveBoardMessageEntity;
import lombok.Data;

/**
 * A board message. Carries only the author's UUID + display name — no username/email.
 */
@Data
public class LiveBoardMessageDto {
  private Long id;
  private Long sessionId;
  private String authorUserId;
  private String authorName;
  private String content;
  private Boolean official;
  private Date createDate;

  public static LiveBoardMessageDto fromEntity(LiveBoardMessageEntity e) {
    if (e == null) return null;
    LiveBoardMessageDto dto = new LiveBoardMessageDto();
    dto.setId(e.getId());
    dto.setSessionId(e.getSessionId());
    dto.setAuthorUserId(e.getAuthorUserId());
    dto.setAuthorName(e.getAuthorName());
    dto.setContent(e.getContent());
    dto.setOfficial(Boolean.TRUE.equals(e.getOfficial()));
    dto.setCreateDate(e.getCreateDate());
    return dto;
  }
}
