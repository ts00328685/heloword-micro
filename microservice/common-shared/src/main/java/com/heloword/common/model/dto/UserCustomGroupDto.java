package com.heloword.common.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class UserCustomGroupDto {
  private Long id;
  private String name;
  private String description;
  private String language;
  private String tags;
  private Integer wordCount;
  private Date createDate;
}
