package com.heloword.common.entity.board;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A song in a session's setlist. Admin manages the list and marks "sung";
 * the audience can also toggle / request (last-write-wins).
 */
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "live_board_song_seq")
@Table(name = "LIVE_BOARD_SONG")
public class LiveBoardSongEntity extends BaseEntity {

  private Long sessionId;

  private String title;

  /** Whether this song has been sung. */
  private Boolean sung;

  /** How many times the audience has requested this song. */
  private Integer requestCount;

  /** Display order in the setlist. */
  private Integer sortOrder;
}
