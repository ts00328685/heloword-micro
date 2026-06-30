package com.heloword.common.model.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A control/event broadcast on /topic/board/{sessionId}/events.
 * type ∈ { DELETE, MUTE, UNMUTE, SESSION_ENDED, SESSION_RESTARTED, PRESENCE, LIKE }.
 * Only UUID-based ids + display names are ever included.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveBoardEventDto {
  private String type;
  private Long sessionId;
  /** DELETE: the removed message id. */
  private Long messageId;
  /** MUTE/UNMUTE: affected user's UUID. */
  private String userId;
  /** MUTE: affected user's display name. */
  private String userName;
  /** PRESENCE: current live viewer count. */
  private Integer presence;
  /** LIKE: the message's new total like count. */
  private Integer likeCount;
}
