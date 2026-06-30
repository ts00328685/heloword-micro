package com.heloword.common.entity.board;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A live busking message-board session. Only one session is in the ACTIVE state at a time.
 * Identity of the creator is stored as the member UUID (never username/email).
 */
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "live_board_session_seq")
@Table(name = "LIVE_BOARD_SESSION")
public class LiveBoardSessionEntity extends BaseEntity {

  /** Admin-chosen session name shown to the audience. */
  private String name;

  /** Lifecycle state: ACTIVE | ENDED. NOT BaseEntity.status (an Integer). */
  private String boardState;

  /** Creating admin's member UUID (never username/email). */
  private String createdByUserId;

  /** Creating admin's display name (nickname). */
  private String createdByName;

  /** When the session was ended (null while active). */
  private Date endedDate;
}
