package com.heloword.common.model.dto;

import lombok.Data;

@Data
public class FriendDto {
  /** Database id of the FriendEntity */
  private Long id;
  private String requesterUsername;
  private String addresseeUsername;
  /** PENDING / ACCEPTED */
  private String friendStatus;
  private String requesterNickname;
  private String addresseeNickname;
}
