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

  /** Whether this song is being performed right now (typically one at a time). */
  private Boolean performing;

  /** How many times the audience has requested this song. */
  private Integer requestCount;

  /** Display order in the setlist. */
  private Integer sortOrder;

  /**
   * Host-private performance note (key, capo, cue, who requested it). Only ever
   * returned to an ADMIN caller — stripped from audience payloads and from the
   * setlist broadcast.
   */
  @Column(length = 500)
  private String note;
}
