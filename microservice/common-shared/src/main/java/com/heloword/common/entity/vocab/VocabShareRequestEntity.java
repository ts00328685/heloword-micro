package com.heloword.common.entity.vocab;

import javax.persistence.Column;
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
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "vocab_share_request_seq")
@Table(name = "VOCAB_SHARE_REQUEST")
public class VocabShareRequestEntity extends BaseEntity {

  private String fromUsername;
  private String toUsername;

  /** ID of the sender's original group — used to copy words at accept time */
  private Long sourceGroupId;

  private String groupName;
  private String description;
  private String language;
  private Integer wordCount;

  /** PENDING | ACCEPTED | REJECTED */
  private String shareStatus;
}
