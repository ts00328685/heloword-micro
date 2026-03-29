package com.heloword.frontendapi.model.challenge;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeEventDto {
  /** ROOM_UPDATE | GAME_STARTED | QUESTION | ROUND_WIN | WRONG_ANSWER | QUESTION_TIMEOUT | GAME_OVER */
  private String type;

  /** full room snapshot — included in ROOM_UPDATE, GAME_STARTED, GAME_OVER */
  private ChallengeRoomDto room;

  // QUESTION fields
  private Integer roundNumber;
  private Integer totalRounds;
  private String question;      // the clue (Chinese translation)
  private String questionId;    // UUID to match answers against
  private Integer timeoutSeconds;
  private String hint;          // e.g. "t _ _ _" — first letter + underscores

  // ROUND_WIN / QUESTION_TIMEOUT fields
  private String winnerId;
  private String winnerName;
  private String correctAnswer;
  private Integer pointsAwarded;  // points the winner earned (1–3 based on word length)
  private Map<String, Integer> scores;

  /** Four answer choices sent with QUESTION events in MULTI_CHOICE rooms */
  private List<String> choices;

  // WRONG_ANSWER fields
  private String targetUserId;  // the player who gave the wrong answer
}
