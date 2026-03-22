package com.heloword.frontendapi.model.challenge;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeEventDto {
  /** ROOM_UPDATE | GAME_STARTED | QUESTION | ROUND_WIN | QUESTION_TIMEOUT | GAME_OVER */
  private String type;

  /** full room snapshot — included in ROOM_UPDATE, GAME_STARTED, GAME_OVER */
  private ChallengeRoomDto room;

  // QUESTION fields
  private Integer roundNumber;
  private Integer totalRounds;
  private String question;      // the clue (Chinese translation)
  private String questionId;    // UUID to match answers against
  private Integer timeoutSeconds;

  // ROUND_WIN / QUESTION_TIMEOUT fields
  private String winnerId;
  private String winnerName;
  private String correctAnswer;
  private Map<String, Integer> scores;
}
