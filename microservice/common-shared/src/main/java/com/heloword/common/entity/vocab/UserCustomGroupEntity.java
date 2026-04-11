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
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "user_custom_group_seq")
@Table(name = "USER_CUSTOM_GROUP")
public class UserCustomGroupEntity extends BaseEntity {

  private String username;
  private String name;
  private String description;
  /** Language hint: EN, JA, DE, or user-defined */
  private String language;
  /** Comma-separated tags for search/filtering */
  private String tags;
}
