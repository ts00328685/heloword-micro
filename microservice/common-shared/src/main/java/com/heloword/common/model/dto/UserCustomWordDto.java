package com.heloword.common.model.dto;

import lombok.Data;

@Data
public class UserCustomWordDto {
  private Long id;
  private Long groupId;
  private String word;
  private String translateEn;
  private String translateCh;
  private String sentence;
  private String phonetics;
  /** Set when the word was copied/hearted from a system vocabulary entry */
  private Long sourceWordId;
  private String sourceTableName;
}
