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
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "user_custom_word_seq")
@Table(name = "USER_CUSTOM_WORD")
public class UserCustomWordEntity extends BaseEntity {

  private Long groupId;
  private String username;
  private String word;
  @Column(length = 2048)
  private String translateEn;
  @Column(length = 2048)
  private String translateCh;
  @Column(length = 2048)
  private String sentence;
  private String phonetics;
  /** ID of the originating system word if copied from the vocabulary list */
  private Long sourceWordId;
  /** Table name of the originating system word (e.g. WORD_ENGLISH) */
  private String sourceTableName;
}
