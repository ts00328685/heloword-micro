package com.heloword.common.entity.record;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Stores a user's manual review-level override for a word group.
 * groupKey = "type:min:max" (e.g. "wordEnglishList:1:50").
 * When present, computeGroupStates uses levelOverride + setAt instead of
 * deriving the level from completion history.
 */
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "record_quiz_group_override_seq")
@Table(name = "RECORD_QUIZ_GROUP_OVERRIDE")
public class RecordQuizGroupOverrideEntity extends BaseEntity {
  private String username;

  @Column(name = "group_key")
  private String groupKey;

  private Integer levelOverride;
  private Date setAt;
}
