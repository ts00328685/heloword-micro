package com.heloword.common.base.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public class BaseWordEntity extends BaseEntity {

  private String word;
  @Column(length = 2048)
  private String translateEn;
  @Column(length = 2048)
  private String translateCh;
  private String level;
  private String tag;
  private String type;
  private String note;
  private String info;
  private String language;

}
