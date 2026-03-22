package com.heloword.frontendapi.service.challenge;

import java.util.List;
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
  private volatile String status; // WAITING, PLAYING, FINISHED
  private boolean system;
  private int totalRounds;
  private volatile int currentRound;
  private volatile String currentQuestionId;
  private volatile String currentCorrectAnswer;
  private volatile String currentQuestion;
  @Builder.Default
  private ConcurrentHashMap<String, ChallengePlayerState> players = new ConcurrentHashMap<>();
  private List<ChallengeQuestion> questions; // shuffled pool
  private volatile ScheduledFuture<?> questionTimer;
}
