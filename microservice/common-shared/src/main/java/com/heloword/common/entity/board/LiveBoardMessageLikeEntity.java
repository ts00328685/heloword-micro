package com.heloword.common.entity.board;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A like on a board message. One row per (messageId, userId) — likes are by
 * member/guest UUID only, enabling toggle + an accurate per-message count.
 */
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "live_board_message_like_seq")
@Table(name = "LIVE_BOARD_MESSAGE_LIKE")
public class LiveBoardMessageLikeEntity extends BaseEntity {

  private Long messageId;

  /** Liker's member/guest UUID. */
  private String userId;
}
