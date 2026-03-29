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
import java.util.stream.Stream;
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

  private static final int QUESTION_TIMEOUT_SECONDS = 10;
  private static final int NEXT_QUESTION_DELAY_SECONDS = 3;
  private static final int SYSTEM_RESTART_DELAY_SECONDS = 10;
  private static final int MAX_POOL_SIZE = 200;

  private static final String[][] SYSTEM_ROOMS = {
    // { id, name, gameType, minId, maxId, gameFormat }
    { "system-easy",         "English Words — Easy (1–2,000)",            "wordEnglishList", "1",    "2000", "TYPING"       },
    { "system-medium",       "English Words — Medium (2,001–4,000)",       "wordEnglishList", "2001", "4000", "TYPING"       },
    { "system-intermediate", "English Words — Intermediate (4,001–6,421)", "wordEnglishList", "4001", "6421", "TYPING"       },
    { "system-mc-easy",         "Multi-Choice — Easy (1–2,000)",            "wordEnglishList", "1",    "2000", "MULTI_CHOICE" },
    { "system-mc-medium",       "Multi-Choice — Medium (2,001–4,000)",       "wordEnglishList", "2001", "4000", "MULTI_CHOICE" },
    { "system-mc-intermediate", "Multi-Choice — Intermediate (4,001–6,421)", "wordEnglishList", "4001", "6421", "MULTI_CHOICE" },
  };

  @Autowired
  private ServiceWordClient serviceWordClient;

  @Autowired
  private ChallengePushService pushService;

  private final ConcurrentHashMap<String, ChallengeRoomState> rooms = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, List<ChallengeQuestion>> wordCache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

  @PostConstruct
  public void init() {
    for (String[] cfg : SYSTEM_ROOMS) {
      String id         = cfg[0];
      String name       = cfg[1];
      String gameType   = cfg[2];
      int    minId      = Integer.parseInt(cfg[3]);
      int    maxId      = Integer.parseInt(cfg[4]);
      String gameFormat = cfg.length > 5 ? cfg[5] : "TYPING";
      ChallengeRoomState room = ChallengeRoomState.builder()
          .id(id).name(name).hostUserId(id)
          .gameType(gameType).gameFormat(gameFormat)
          .status("WAITING").system(true)
          .totalRounds(10).wordMinId(minId).wordMaxId(maxId)
          .build();
      rooms.put(id, room);
    }
    log.info("Challenge system rooms initialized ({} rooms)", SYSTEM_ROOMS.length);
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
      scheduler.schedule(() -> startGame(roomId, roomId), 3, TimeUnit.SECONDS);
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

    // MULTI_CHOICE: each player gets exactly one attempt per question
    boolean isMultiChoice = "MULTI_CHOICE".equals(room.getGameFormat());
    if (isMultiChoice && !room.getCurrentQuestionAnsweredPlayers().add(answer.getUserId())) {
      return; // already answered this question
    }

    String normalized = answer.getAnswer().toLowerCase().trim();

    if (!normalized.equals(room.getCurrentCorrectAnswer())) {
      // Wrong answer — deduct 1 point (floor at -999) and notify room
      ChallengePlayerState player = room.getPlayers().get(answer.getUserId());
      player.setScore(Math.max(-999, player.getScore() - 1));
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
    boolean isMultiChoice = "MULTI_CHOICE".equals(room.getGameFormat());
    String hint = isMultiChoice ? null : buildHint(q.getNormalizedAnswer());
    List<String> choices = buildChoices(room, q);

    room.setCurrentRound(round);
    room.setCurrentQuestionId(q.getId());
    room.setCurrentCorrectAnswer(q.getNormalizedAnswer());
    room.setCurrentQuestion(q.getQuestion());
    room.setCurrentHint(hint);
    room.setCurrentChoices(choices);
    room.setQuestionStartTime(System.currentTimeMillis());
    room.getCurrentQuestionAnsweredPlayers().clear();

    pushService.broadcastRoomEvent(room.getId(), ChallengeEventDto.builder()
        .type("QUESTION")
        .roundNumber(round)
        .totalRounds(room.getTotalRounds())
        .question(q.getQuestion())
        .questionId(q.getId())
        .timeoutSeconds(QUESTION_TIMEOUT_SECONDS)
        .hint(hint)
        .choices(choices)
        .build());

    // Schedule timeout
    room.setQuestionTimer(scheduler.schedule(() -> handleQuestionTimeout(room, q.getId()), QUESTION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
  }

  /** Builds a shuffled list of 4 choices (1 correct + 3 distractors) for MULTI_CHOICE rooms.
   *  Returns null for TYPING rooms. */
  private List<String> buildChoices(ChallengeRoomState room, ChallengeQuestion correct) {
    if (!"MULTI_CHOICE".equals(room.getGameFormat())) return null;
    List<ChallengeQuestion> pool = room.getQuestions();
    if (pool == null || pool.size() < 4) return null;

    // Pick 3 random distractors — different normalized answer from correct
    List<String> candidates = pool.stream()
        .filter(q -> !q.getNormalizedAnswer().equals(correct.getNormalizedAnswer()))
        .map(ChallengeQuestion::getDisplayAnswer)
        .distinct()
        .collect(Collectors.toList());
    Collections.shuffle(candidates);
    List<String> distractors = candidates.stream().limit(3).collect(Collectors.toList());

    if (distractors.size() < 3) return null;

    List<String> choices = new ArrayList<>(Stream.concat(
        Stream.of(correct.getDisplayAnswer()),
        distractors.stream()
    ).collect(Collectors.toList()));
    Collections.shuffle(choices);
    return choices;
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
          startGame(room.getId(), room.getId());
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

    ChallengeRoomDto.ChallengeRoomDtoBuilder builder = ChallengeRoomDto.builder()
        .id(room.getId()).name(room.getName()).hostUserId(room.getHostUserId())
        .gameType(room.getGameType()).gameFormat(room.getGameFormat())
        .status(room.getStatus()).system(room.isSystem())
        .totalRounds(room.getTotalRounds()).currentRound(room.getCurrentRound())
        .players(playerList);

    // Include active-question snapshot for late joiners
    if ("PLAYING".equals(room.getStatus()) && room.getCurrentQuestionId() != null) {
      int elapsed = (int) ((System.currentTimeMillis() - room.getQuestionStartTime()) / 1000);
      int remaining = Math.max(0, QUESTION_TIMEOUT_SECONDS - elapsed);
      builder.currentQuestion(room.getCurrentQuestion())
             .currentQuestionId(room.getCurrentQuestionId())
             .currentHint(room.getCurrentHint())
             .remainingSeconds(remaining)
             .currentChoices(room.getCurrentChoices());
    }
    return builder.build();
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
