package com.heloword.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "WORD_GERMAN")
public class WordGermanEntity extends BaseEntity {

  @Generated
  private Long id;
  private String word;
  private String phonetics;
  private String level;

}
