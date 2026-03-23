package com.heloword.frontendapi.service.challenge.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.entity.BaseWordEntity;
import com.heloword.common.feignclient.ServiceWordClient;
import com.heloword.frontendapi.model.challenge.ChallengeAnswerDto;
import com.heloword.frontendapi.model.challenge.ChallengeEventDto;
import com.heloword.frontendapi.model.challenge.ChallengePlayerDto;
import com.heloword.frontendapi.model.challenge.ChallengeRoomDto;
import com.heloword.frontendapi.model.challenge.CreateRoomRequest;
import com.heloword.frontendapi.model.challenge.JoinRoomRequest;
import com.heloword.frontendapi.service.challenge.ChallengePlayerState;
import com.heloword.frontendapi.service.challenge.ChallengePushService;
import com.heloword.frontendapi.service.challenge.ChallengeQuestion;
import com.heloword.frontendapi.service.challenge.ChallengeRoomState;
import com.heloword.frontendapi.service.challenge.ChallengeService;

@Log4j2
@Service
public class ChallengeServiceImpl implements ChallengeService {

  private static final String SYSTEM_ROOM_ID = "system";
  private static final int QUESTION_TIMEOUT_SECONDS = 10;
  private static final int NEXT_QUESTION_DELAY_SECONDS = 3;
  private static final int SYSTEM_RESTART_DELAY_SECONDS = 10;
  private static final int MAX_POOL_SIZE = 200;
  private static final int SYSTEM_WORD_MIN_ID = 3000;
  private static final int SYSTEM_WORD_MAX_ID = 6421;

  @Autowired
  private ServiceWordClient serviceWordClient;

  @Autowired
  private ChallengePushService pushService;

  private final ConcurrentHashMap<String, ChallengeRoomState> rooms = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, List<ChallengeQuestion>> wordCache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

  @PostConstruct
  public void init() {
    ChallengeRoomState systemRoom = ChallengeRoomState.builder()
        .id(SYSTEM_ROOM_ID)
        .name("System Room — English Words")
        .hostUserId(SYSTEM_ROOM_ID)
        .gameType("wordEnglishList")
        .status("WAITING")
        .system(true)
        .totalRounds(10)
        .wordMinId(SYSTEM_WORD_MIN_ID)
        .wordMaxId(SYSTEM_WORD_MAX_ID)
        .build();
    rooms.put(SYSTEM_ROOM_ID, systemRoom);
    log.info("Challenge system room initialized");
  }

  @PreDestroy
  public void destroy() {
    scheduler.shutdownNow();
  }

  @Override
  public List<ChallengeRoomDto> listRooms() {
    return rooms.values().stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  public ChallengeRoomDto createRoom(String hostUserId, String hostDisplayName, CreateRoomRequest req) {
    int rounds = Math.min(20, Math.max(5, req.getTotalRounds() == 0 ? 10 : req.getTotalRounds()));
    String id = UUID.randomUUID().toString();
    ChallengeRoomState room = ChallengeRoomState.builder()
        .id(id)
        .name(StringUtils.isBlank(req.getName()) ? hostDisplayName + "'s Room" : req.getName().trim())
        .hostUserId(hostUserId)
        .gameType(req.getGameType())
        .status("WAITING")
        .system(false)
        .totalRounds(rounds)
        .build();
    // Add host as first player
    room.getPlayers().put(hostUserId, ChallengePlayerState.builder()
        .userId(hostUserId).displayName(hostDisplayName).score(0).guest(false).build());
    rooms.put(id, room);
    broadcastRoomList();
    return toDto(room);
  }

  @Override
  public ChallengeRoomDto joinRoom(String roomId, JoinRoomRequest req) {
    ChallengeRoomState room = rooms.get(roomId);
    if (room == null) throw new IllegalArgumentException("Room not found");
    room.getPlayers().putIfAbsent(req.getUserId(), ChallengePlayerState.builder()
        .userId(req.getUserId()).displayName(req.getDisplayName()).score(0).guest(req.isGuest()).build());
    broadcastRoomList();
    pushService.broadcastRoomEvent(roomId, ChallengeEventDto.builder()
        .type("ROOM_UPDATE").room(toDto(room)).build());
    // Auto-start system room when first real player joins and room is WAITING
    if (room.isSystem() && "WAITING".equals(room.getStatus()) && room.getPlayers().size() >= 1) {
      scheduler.schedule(() -> startGame(roomId, SYSTEM_ROOM_ID), 3, TimeUnit.SECONDS);
    }
    return toDto(room);
  }

  @Override
  public void leaveRoom(String roomId, String userId) {
    ChallengeRoomState room = rooms.get(roomId);
    if (room == null) return;
    room.getPlayers().remove(userId);
    // Non-system rooms with no players get cleaned up
    if (!room.isSystem() && room.getPlayers().isEmpty()) {
      cancelTimer(room);
      rooms.remove(roomId);
    } else {
      pushService.broadcastRoomEvent(roomId, ChallengeEventDto.builder()
          .type("ROOM_UPDATE").room(toDto(room)).build());
    }
    broadcastRoomList();
  }

  @Override
  public void startGame(String roomId, String requestingUserId) {
    ChallengeRoomState room = rooms.get(roomId);
    if (room == null) return;
    if (!room.isSystem() && !room.getHostUserId().equals(requestingUserId)) return;
    if ("PLAYING".equals(room.getStatus())) return;

    List<ChallengeQuestion> pool = loadWordPool(room.getGameType(), room.getWordMinId(), room.getWordMaxId());
    if (pool.isEmpty()) {
      log.warn("No questions available for gameType={}", room.getGameType());
      return;
    }

    List<ChallengeQuestion> shuffled = new ArrayList<>(pool);
    Collections.shuffle(shuffled);
    room.setQuestions(shuffled);
    room.setCurrentRound(0);
    room.setStatus("PLAYING");
    // Reset scores
    room.getPlayers().values().forEach(p -> p.setScore(0));

    pushService.broadcastRoomEvent(roomId, ChallengeEventDto.builder()
        .type("GAME_STARTED").room(toDto(room)).build());
    broadcastRoomList();
    scheduleNextQuestion(room, 1);
  }

  @Override
  public void processAnswer(String roomId, ChallengeAnswerDto answer) {
    ChallengeRoomState room = rooms.get(roomId);
    if (room == null || !"PLAYING".equals(room.getStatus())) return;
    if (!answer.getQuestionId().equals(room.getCurrentQuestionId())) return;
    if (StringUtils.isBlank(answer.getAnswer())) return;

    // Ensure player exists (late joiners)
    room.getPlayers().computeIfAbsent(answer.getUserId(), uid ->
        ChallengePlayerState.builder().userId(uid).displayName(answer.getDisplayName())
            .score(0).guest(answer.isGuest()).build());

    String normalized = answer.getAnswer().toLowerCase().trim();

    if (!normalized.equals(room.getCurrentCorrectAnswer())) {
      // Wrong answer — deduct 1 point (floor at -999) and notify room
      ChallengePlayerState player = room.getPlayers().get(answer.getUserId());
      player.setScore(player.getScore() - 1);
      pushService.broadcastRoomEvent(roomId, ChallengeEventDto.builder()
          .type("WRONG_ANSWER")
          .targetUserId(answer.getUserId())
          .scores(buildScores(room))
          .build());
      return;
    }

    // Correct answer — only first one wins (synchronized double-win guard)
    synchronized (room) {
      if (!answer.getQuestionId().equals(room.getCurrentQuestionId())) return;
      room.setCurrentQuestionId(null);
    }

    cancelTimer(room);
    int points = pointsForWord(room.getCurrentCorrectAnswer());
    room.getPlayers().get(answer.getUserId()).setScore(
        room.getPlayers().get(answer.getUserId()).getScore() + points);

    Map<String, Integer> scores = buildScores(room);
    pushService.broadcastRoomEvent(roomId, ChallengeEventDto.builder()
        .type("ROUND_WIN")
        .winnerId(answer.getUserId())
        .winnerName(answer.getDisplayName())
        .correctAnswer(room.getCurrentCorrectAnswer())
        .pointsAwarded(points)
        .scores(scores)
        .build());
    broadcastRoomList();

    if (room.getCurrentRound() >= room.getTotalRounds()) {
      scheduleGameOver(room);
    } else {
      scheduleNextQuestion(room, NEXT_QUESTION_DELAY_SECONDS);
    }
  }

  // ── private helpers ───────────────────────────────────────────────────────

  private void scheduleNextQuestion(ChallengeRoomState room, int delaySeconds) {
    room.setQuestionTimer(scheduler.schedule(() -> sendNextQuestion(room), delaySeconds, TimeUnit.SECONDS));
  }

  private void sendNextQuestion(ChallengeRoomState room) {
    if (!"PLAYING".equals(room.getStatus())) return;
    int round = room.getCurrentRound() + 1;
    if (round > room.getTotalRounds() || room.getQuestions() == null || round > room.getQuestions().size()) {
      scheduleGameOver(room);
      return;
    }
    ChallengeQuestion q = room.getQuestions().get(round - 1);
    room.setCurrentRound(round);
    room.setCurrentQuestionId(q.getId());
    room.setCurrentCorrectAnswer(q.getNormalizedAnswer());
    room.setCurrentQuestion(q.getQuestion());

    pushService.broadcastRoomEvent(room.getId(), ChallengeEventDto.builder()
        .type("QUESTION")
        .roundNumber(round)
        .totalRounds(room.getTotalRounds())
        .question(q.getQuestion())
        .questionId(q.getId())
        .timeoutSeconds(QUESTION_TIMEOUT_SECONDS)
        .hint(buildHint(q.getNormalizedAnswer()))
        .build());

    // Schedule timeout
    room.setQuestionTimer(scheduler.schedule(() -> handleQuestionTimeout(room, q.getId()), QUESTION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
  }

  private void handleQuestionTimeout(ChallengeRoomState room, String questionId) {
    synchronized (room) {
      if (!questionId.equals(room.getCurrentQuestionId())) return;
      room.setCurrentQuestionId(null);
    }
    pushService.broadcastRoomEvent(room.getId(), ChallengeEventDto.builder()
        .type("QUESTION_TIMEOUT")
        .correctAnswer(room.getCurrentCorrectAnswer())
        .scores(buildScores(room))
        .build());
    if (room.getCurrentRound() >= room.getTotalRounds()) {
      scheduleGameOver(room);
    } else {
      scheduleNextQuestion(room, NEXT_QUESTION_DELAY_SECONDS);
    }
  }

  private void scheduleGameOver(ChallengeRoomState room) {
    room.setStatus("FINISHED");
    pushService.broadcastRoomEvent(room.getId(), ChallengeEventDto.builder()
        .type("GAME_OVER")
        .room(toDto(room))
        .scores(buildScores(room))
        .build());
    broadcastRoomList();
    if (room.isSystem()) {
      room.setQuestionTimer(scheduler.schedule(() -> {
        if (!room.getPlayers().isEmpty()) {
          startGame(room.getId(), SYSTEM_ROOM_ID);
        } else {
          room.setStatus("WAITING");
          broadcastRoomList();
        }
      }, SYSTEM_RESTART_DELAY_SECONDS, TimeUnit.SECONDS));
    }
  }

  private void cancelTimer(ChallengeRoomState room) {
    if (room.getQuestionTimer() != null && !room.getQuestionTimer().isDone()) {
      room.getQuestionTimer().cancel(false);
    }
  }

  private Map<String, Integer> buildScores(ChallengeRoomState room) {
    return room.getPlayers().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getScore()));
  }

  private List<ChallengeQuestion> loadWordPool(String gameType, int minId, int maxId) {
    String cacheKey = (minId > 0 || maxId > 0) ? gameType + ":" + minId + ":" + maxId : gameType;
    return wordCache.computeIfAbsent(cacheKey, k -> {
      try {
        List<? extends BaseWordEntity> words = fetchWords(gameType);
        List<ChallengeQuestion> questions = words.stream()
            .filter(w -> {
              if (minId <= 0 && maxId <= 0) return true;
              long id = w.getId();
              return id >= minId && id <= maxId;
            })
            .map(this::toQuestion)
            .filter(q -> q != null)
            .limit(MAX_POOL_SIZE)
            .collect(Collectors.toList());
        Collections.shuffle(questions);
        log.info("Loaded {} questions for cacheKey={}", questions.size(), cacheKey);
        return questions;
      } catch (Exception e) {
        log.error("Failed to load word pool for cacheKey={}", cacheKey, e);
        return new ArrayList<>();
      }
    });
  }

  private List<? extends BaseWordEntity> fetchWords(String gameType) {
    switch (gameType) {
      case "wordEnglishList": return serviceWordClient.getAllEnWords().getData();
      case "wordGermanList":  return serviceWordClient.getAllGeWords().getData();
      case "wordJapaneseList": return serviceWordClient.getAllJpWords().getData();
      default: return serviceWordClient.getAllEnWords().getData();
    }
  }

  private ChallengeQuestion toQuestion(BaseWordEntity entity) {
    if (StringUtils.isBlank(entity.getWord())) return null;
    String question = StringUtils.isNotBlank(entity.getTranslateCh())
        ? entity.getTranslateCh().split("[；;,，]")[0].trim()
        : entity.getTranslateEn();
    if (StringUtils.isBlank(question)) return null;
    return ChallengeQuestion.builder()
        .id(UUID.randomUUID().toString())
        .question(question)
        .normalizedAnswer(entity.getWord().toLowerCase().trim())
        .displayAnswer(entity.getWord())
        .build();
  }

  private ChallengeRoomDto toDto(ChallengeRoomState room) {
    List<ChallengePlayerDto> playerList = room.getPlayers().values().stream()
        .map(p -> ChallengePlayerDto.builder()
            .userId(p.getUserId()).displayName(p.getDisplayName())
            .score(p.getScore()).isGuest(p.isGuest()).build())
        .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
        .collect(Collectors.toList());
    return ChallengeRoomDto.builder()
        .id(room.getId()).name(room.getName()).hostUserId(room.getHostUserId())
        .gameType(room.getGameType()).status(room.getStatus()).system(room.isSystem())
        .totalRounds(room.getTotalRounds()).currentRound(room.getCurrentRound())
        .players(playerList).build();
  }

  /** Builds a hint string: first and last letter of each word visible, middle as underscores.
   *  e.g. "tide" → "t _ _ e", "new york" → "n _ w   y _ _ k", "hi" → "h i", "a" → "a" */
  private String buildHint(String answer) {
    if (StringUtils.isBlank(answer)) return "";
    String[] words = answer.trim().split(" ");
    StringBuilder sb = new StringBuilder();
    for (int w = 0; w < words.length; w++) {
      if (w > 0) sb.append("   ");
      String word = words[w];
      int len = word.length();
      for (int i = 0; i < len; i++) {
        if (i > 0) sb.append(" ");
        if (i == 0 || i == len - 1) {
          sb.append(word.charAt(i));
        } else {
          sb.append('_');
        }
      }
    }
    return sb.toString();
  }

  /** +1 for 1–4 chars, +2 for 5–7 chars, +3 for 8+ chars */
  private int pointsForWord(String word) {
    if (word == null) return 1;
    int len = word.trim().length();
    if (len >= 8) return 3;
    if (len >= 5) return 2;
    return 1;
  }

  private void broadcastRoomList() {
    pushService.broadcastRoomList(listRooms());
  }
}
