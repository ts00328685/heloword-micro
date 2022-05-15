package com.heloword.common.entity.record;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "record_quiz_seq")
@Table(name = "RECORD_QUIZ")
public class RecordQuizEntity extends BaseEntity {
  private String username;
  private Long answerId;
  private String answerTableName;
  /** seconds */
  private Integer timeSpent;
  private Date startTime;
  private Date finishedTime;
  private Integer pronounceCount;
  private Integer deleteCount;
  private Integer wrongCount;
  private Long recordQuizSettingId;
}
