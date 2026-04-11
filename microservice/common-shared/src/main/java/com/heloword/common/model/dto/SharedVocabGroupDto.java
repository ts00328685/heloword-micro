package com.heloword.common.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class SharedVocabGroupDto {
  private Long id;
  private Long groupId;
  private String name;
  private String description;
  private String language;
  private String tags;
  private Integer wordCount;
  /** UUID of the user who shared — never expose username */
  private String sharerUuid;
  /** Display name of the sharer */
  private String sharerDisplayName;
  /** PENDING | APPROVED | REJECTED */
  private String shareStatus;
  private Date createDate;
}
