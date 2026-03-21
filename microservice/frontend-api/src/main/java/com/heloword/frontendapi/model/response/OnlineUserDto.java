package com.heloword.frontendapi.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnlineUserDto {
  private String userId;
  private String displayName;
  private Boolean isGuest;
}
