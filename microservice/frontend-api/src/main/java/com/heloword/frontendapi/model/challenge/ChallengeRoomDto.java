package com.heloword.frontendapi.model.challenge;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeRoomDto {
  private String id;
  private String name;
  private String hostUserId;
  private String gameType;
  private String status; // WAITING, PLAYING, FINISHED
  private boolean system;
  private int totalRounds;
  private int currentRound;
  private List<ChallengePlayerDto> players;
}
