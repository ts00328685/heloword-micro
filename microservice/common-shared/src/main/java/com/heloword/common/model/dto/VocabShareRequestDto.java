package com.heloword.common.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class VocabShareRequestDto {
  private Long id;
  private String fromUsername;
  private String toUsername;
  /** ID of the sender's source group — set by the sender's client when calling sendShare */
  private Long sourceGroupId;
  private String groupName;
  private String description;
  private String language;
  private Integer wordCount;
  /** PENDING | ACCEPTED | REJECTED */
  private String shareStatus;
  private Date createDate;
}
