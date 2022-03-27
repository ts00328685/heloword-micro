package com.heloword.common.base.entity;

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
  private String translateEn;
  private String translateCh;
  private String level;
  private String tag;
  private String type;
  private String note;
  private String info;
  private String language;

}
