package com.heloword.common.entity.board;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A single message on a live board. Author is identified by member/guest UUID only
 * (never username/email); only the display name is surfaced to other users.
 */
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "live_board_message_seq")
@Table(name = "LIVE_BOARD_MESSAGE")
public class LiveBoardMessageEntity extends BaseEntity {

  private Long sessionId;

  /** Author identifier: member UUID or guest UUID. */
  private String authorUserId;

  /** Author display name (nickname / guest-typed name). */
  private String authorName;

  @Column(length = 4096)
  private String content;

  /** Official (admin-pinned) message shown at the top of the board. */
  private Boolean official;

  /** Soft-delete flag: hidden from the board but kept in history. */
  private Boolean deleted;
}
