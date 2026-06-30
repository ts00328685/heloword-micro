package com.heloword.common.model.dto.board;

import java.util.List;
import lombok.Data;

/**
 * Full state of a session for the initial board load: the session, its visible
 * (non-deleted) messages, its setlist, and the list of muted UUIDs.
 */
@Data
public class LiveBoardSnapshotDto {
  private LiveBoardSessionDto session;
  private List<LiveBoardMessageDto> messages;
  private List<LiveBoardSongDto> songs;
  private List<String> mutedUserIds;
  /** Message ids the requesting user has liked. */
  private List<Long> likedMessageIds;
}
