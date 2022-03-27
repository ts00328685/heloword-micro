package com.heloword.common.base.entity;

import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
public class BaseSentenceEntity extends BaseWordEntity {

  private String sentence;

}
