package com.heloword.common.entity.board;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A muted user within a session. Muting is by member/guest UUID only.
 */
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "live_board_mute_seq")
@Table(name = "LIVE_BOARD_MUTE")
public class LiveBoardMuteEntity extends BaseEntity {

  private Long sessionId;

  /** Muted user's member/guest UUID. */
  private String userId;

  /** Display name at the time of muting (for the admin's reference). */
  private String mutedName;
}
