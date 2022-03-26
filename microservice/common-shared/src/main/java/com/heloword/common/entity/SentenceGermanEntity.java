package com.heloword.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
@Table(name = "SENTENCE_GERMAN")
public class SentenceGermanEntity extends BaseEntity {

  @Generated()
  private Long id;
  @NotNull
  private String sentence;
  private String translateEn;
  private String translateCh;
  private String level;
  private String tag;
  private String type;
  private String note;
  private String info;

}
