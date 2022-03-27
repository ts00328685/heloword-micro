package com.heloword.common.base.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
public class BaseSentenceEntity extends BaseWordEntity {

  @Column(length = 2048)
  private String sentence;

}
