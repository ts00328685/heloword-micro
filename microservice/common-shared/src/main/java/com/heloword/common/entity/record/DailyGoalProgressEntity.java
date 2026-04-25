package com.heloword.common.entity.record;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import com.heloword.common.base.entity.BaseEntity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "daily_goal_progress_seq")
@Table(name = "DAILY_GOAL_PROGRESS",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userUuid", "date"}))
public class DailyGoalProgressEntity extends BaseEntity {

  @Column(nullable = false, length = 36)
  private String userUuid;

  @Column(nullable = false)
  private String username;

  /** YYYY-MM-DD */
  @Column(nullable = false, length = 10)
  private String date;

  // Japanese progress
  private int jpQuizWords;
  private int jpScrambleSentences;
  private int jpSpokenSentences;
  private int jpSpokenScoreTotal;
  private int jpWrittenSentences;

  // English progress
  private int enQuizWords;
  private int enScrambleSentences;
  private int enSpokenSentences;
  private int enSpokenScoreTotal;
  private int enWrittenSentences;
}
