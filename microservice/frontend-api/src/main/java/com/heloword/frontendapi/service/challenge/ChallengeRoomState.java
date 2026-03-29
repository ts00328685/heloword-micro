package com.heloword.frontendapi.service.challenge;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeRoomState {
  private String id;
  private String name;
  private String hostUserId;
  private String gameType;
  /** TYPING (default) or MULTI_CHOICE */
  private String gameFormat;
  private volatile String status; // WAITING, PLAYING, FINISHED
  private boolean system;
  private int totalRounds;
  private volatile int currentRound;
  private volatile String currentQuestionId;
  private volatile String currentCorrectAnswer;
  private volatile String currentQuestion;
  private volatile String currentHint;
  private volatile List<String> currentChoices;
  private volatile long questionStartTime; // System.currentTimeMillis() when question was sent
  @Builder.Default
  private ConcurrentHashMap<String, ChallengePlayerState> players = new ConcurrentHashMap<>();
  /** Tracks players who have already submitted an answer for the current question (MULTI_CHOICE) */
  @Builder.Default
  private Set<String> currentQuestionAnsweredPlayers = ConcurrentHashMap.newKeySet();
  private List<ChallengeQuestion> questions; // shuffled pool
  private volatile ScheduledFuture<?> questionTimer;
  /** Optional word ID range filter applied when loading the pool (0 = no filter) */
  private int wordMinId;
  private int wordMaxId;
}
