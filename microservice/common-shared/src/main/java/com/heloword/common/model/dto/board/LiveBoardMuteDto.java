package com.heloword.common.model.dto.board;

import com.heloword.common.entity.board.LiveBoardMuteEntity;
import lombok.Data;

@Data
public class LiveBoardMuteDto {
  private String userId;
  private String mutedName;

  public static LiveBoardMuteDto fromEntity(LiveBoardMuteEntity e) {
    if (e == null) return null;
    LiveBoardMuteDto dto = new LiveBoardMuteDto();
    dto.setUserId(e.getUserId());
    dto.setMutedName(e.getMutedName());
    return dto;
  }
}
