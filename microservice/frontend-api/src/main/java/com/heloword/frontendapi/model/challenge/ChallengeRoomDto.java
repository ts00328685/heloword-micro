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
  /** TYPING (default) or MULTI_CHOICE */
  private String gameFormat;
  private String status; // WAITING, PLAYING, FINISHED
  private boolean system;
  private int totalRounds;
  private int currentRound;
  private List<ChallengePlayerDto> players;
  // Late-join fields — only set when status=PLAYING and a question is active
  private String currentQuestion;
  private String currentQuestionId;
  private String currentHint;
  private Integer remainingSeconds;
  private List<String> currentChoices;
}
