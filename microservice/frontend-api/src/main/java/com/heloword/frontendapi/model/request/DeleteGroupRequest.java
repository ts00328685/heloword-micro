package com.heloword.frontendapi.model.request;

import lombok.Data;

@Data
public class DeleteGroupRequest {
  private String type;
  private Integer min;
  private Integer max;
}
