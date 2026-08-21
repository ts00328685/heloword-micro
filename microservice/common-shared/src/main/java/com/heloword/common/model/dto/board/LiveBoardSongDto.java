package com.heloword.common.model.dto.board;

import java.util.List;
import java.util.stream.Collectors;
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
  /** Host-private note. Present only on ADMIN responses — see {@link #withoutNote()}. */
  private String note;

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
    dto.setNote(e.getNote());
    return dto;
  }

  /**
   * Audience-facing copy with the host's private note dropped. Hiding the note in
   * the UI alone would still ship it in the JSON, so every payload that can reach
   * a non-admin goes through here.
   */
  public LiveBoardSongDto withoutNote() {
    LiveBoardSongDto dto = new LiveBoardSongDto();
    dto.setId(id);
    dto.setSessionId(sessionId);
    dto.setTitle(title);
    dto.setSung(sung);
    dto.setPerforming(performing);
    dto.setRequestCount(requestCount);
    dto.setSortOrder(sortOrder);
    return dto;
  }

  /** Null-safe {@link #withoutNote()} over a list. */
  public static List<LiveBoardSongDto> withoutNotes(List<LiveBoardSongDto> songs) {
    if (songs == null) return null;
    return songs.stream().map(LiveBoardSongDto::withoutNote).collect(Collectors.toList());
  }
}
