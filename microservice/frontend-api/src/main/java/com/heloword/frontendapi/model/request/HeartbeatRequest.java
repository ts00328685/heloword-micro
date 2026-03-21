package com.heloword.frontendapi.model.request;

import lombok.Data;

@Data
public class HeartbeatRequest {
  private String userId;
  private String displayName;
  private Boolean isGuest;
}
