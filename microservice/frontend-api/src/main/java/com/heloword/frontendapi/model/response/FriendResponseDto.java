package com.heloword.frontendapi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendResponseDto {
  private Long id;
  /** The other user's identifier */
  private String otherUserId;
  /** Display name (nickname if set, otherwise their actual name) */
  private String displayName;
  /** Nickname I assigned to this friend */
  private String myNickname;
  /** PENDING_SENT / PENDING_RECEIVED / ACCEPTED */
  private String status;
  /** Whether this friend is currently online */
  @JsonProperty("isOnline")
  private boolean isOnline;
}
