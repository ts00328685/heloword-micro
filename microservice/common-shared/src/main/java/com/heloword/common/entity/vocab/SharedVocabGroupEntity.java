package com.heloword.common.entity.vocab;

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
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "shared_vocab_group_seq")
@Table(name = "SHARED_VOCAB_GROUP")
public class SharedVocabGroupEntity extends BaseEntity {

  /** FK to USER_CUSTOM_GROUP — the original group submitted by the requester */
  private Long groupId;

  /** Internal use only — never exposed to frontend */
  private String requesterUsername;

  /** Exposed to frontend instead of username */
  private String requesterUuid;

  /** Display name (nickname or fullname) at time of request */
  private String requesterDisplayName;

  private String name;
  private String description;
  private String language;
  private String tags;
  private Integer wordCount;

  /** PENDING | APPROVED | REJECTED */
  private String shareStatus;
}
