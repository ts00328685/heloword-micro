package com.heloword.frontendapi.model.challenge;

import lombok.Data;

@Data
public class JoinRoomRequest {
  private String userId;
  private String displayName;
  private boolean guest;
}
