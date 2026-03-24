package com.heloword.frontendapi.model.request;

import java.util.Date;
import lombok.Data;

@Data
public class GroupOverrideRequest {
  private String type;
  private Integer min;
  private Integer max;
  private Integer levelOverride;
  private Date setAt;
}
