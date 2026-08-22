package com.heloword.frontendapi.service.board.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.board.LiveBoardEventDto;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardMuteDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSnapshotDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;
import com.heloword.common.type.ResponseCode;
import com.heloword.frontendapi.service.board.BoardFrontendService;
import com.heloword.frontendapi.service.board.BoardPushService;

@Log4j2
@Service
@AllArgsConstructor
public class BoardFrontendServiceImpl implements BoardFrontendService {

  private static final String STATE_ACTIVE = "ACTIVE";
  private static final long PRESENCE_WINDOW_MS = 90_000L;

  private static String presenceKey(Long sessionId) {
    return "board:presence:" + sessionId;
  }

  private final RedisTemplate<String, Object> redisTemplate;
  private final ServiceRecordClient serviceRecordClient;
  private final BoardPushService boardPushService;

  @Override
  public LiveBoardSessionDto getActiveSession() {
    return serviceRecordClient.getActiveBoardSession().getData();
  }

  @Override
  public List<LiveBoardSessionDto> getSessions() {
    return serviceRecordClient.getBoardSessions().getData();
  }

  @Override
  public LiveBoardSnapshotDto getSnapshot(Long sessionId, String userId) {
    return serviceRecordClient.getBoardSnapshot(sessionId, userId).getData();
  }

  @Override
  public LiveBoardSessionDto createSession(String name, String creatorUuid, String creatorName) {
    LiveBoardSessionDto req = new LiveBoardSessionDto();
    req.setName(name);
    req.setCreatedByUserId(creatorUuid);
    req.setCreatedByName(creatorName);
    LiveBoardSessionDto created = serviceRecordClient.createBoardSession(req).getData();
    boardPushService.broadcastActive(created);
    return created;
  }

  @Override
  public LiveBoardSessionDto endSession(Long sessionId) {
    LiveBoardSessionDto ended = serviceRecordClient.endBoardSession(sessionId).getData();
    redisTemplate.delete(presenceKey(sessionId));
    boardPushService.sendEvent(sessionId, LiveBoardEventDto.builder()
        .type("SESSION_ENDED").sessionId(sessionId).build());
    boardPushService.broadcastActive(ended);
    return ended;
  }

  @Override
  public LiveBoardSessionDto restartSession(Long sessionId) {
    LiveBoardSessionDto restarted = serviceRecordClient.restartBoardSession(sessionId).getData();
    boardPushService.sendEvent(sessionId, LiveBoardEventDto.builder()
        .type("SESSION_RESTARTED").sessionId(sessionId).build());
    boardPushService.broadcastActive(restarted);
    return restarted;
  }

  @Override
  public LiveBoardMessageDto postMessage(Long sessionId, LiveBoardMessageDto dto) {
    LiveBoardSessionDto active = serviceRecordClient.getActiveBoardSession().getData();
    if (active == null || !active.getId().equals(sessionId)) {
      throw HeloServiceException.of(ResponseCode.INVALID_REQUEST, "This session is not live.");
    }
    Boolean muted = serviceRecordClient.isBoardUserMuted(sessionId, dto.getAuthorUserId()).getData();
    if (Boolean.TRUE.equals(muted)) {
      throw HeloServiceException.of(ResponseCode.INVALID_REQUEST, "You have been muted by the host.");
    }
    dto.setOfficial(false);
    LiveBoardMessageDto saved = serviceRecordClient.addBoardMessage(sessionId, dto).getData();
    boardPushService.sendMessage(sessionId, saved);
    return saved;
  }

  @Override
  public LiveBoardMessageDto postOfficial(Long sessionId, LiveBoardMessageDto dto) {
    dto.setOfficial(true);
    LiveBoardMessageDto saved = serviceRecordClient.addBoardMessage(sessionId, dto).getData();
    boardPushService.sendMessage(sessionId, saved);
    return saved;
  }

  @Override
  public LiveBoardMessageDto deleteMessage(Long messageId) {
    LiveBoardMessageDto deleted = serviceRecordClient.deleteBoardMessage(messageId).getData();
    if (deleted != null && deleted.getSessionId() != null) {
      boardPushService.sendEvent(deleted.getSessionId(), LiveBoardEventDto.builder()
          .type("DELETE").sessionId(deleted.getSessionId()).messageId(deleted.getId()).build());
    }
    return deleted;
  }

  @Override
  public LiveBoardMessageDto toggleLike(Long sessionId, Long messageId, String userId) {
    LiveBoardMessageDto result = serviceRecordClient.toggleBoardMessageLike(messageId, userId).getData();
    if (result != null) {
      boardPushService.sendEvent(sessionId, LiveBoardEventDto.builder()
          .type("LIKE").sessionId(sessionId).messageId(messageId).likeCount(result.getLikeCount()).build());
    }
    return result;
  }

  @Override
  public List<String> mute(Long sessionId, LiveBoardMuteDto dto) {
    List<String> muted = serviceRecordClient.muteBoardUser(sessionId, dto).getData();
    boardPushService.sendEvent(sessionId, LiveBoardEventDto.builder()
        .type("MUTE").sessionId(sessionId).userId(dto.getUserId()).userName(dto.getMutedName()).build());
    return muted;
  }

  @Override
  public List<String> unmute(Long sessionId, String userId) {
    List<String> muted = serviceRecordClient.unmuteBoardUser(sessionId, userId).getData();
    boardPushService.sendEvent(sessionId, LiveBoardEventDto.builder()
        .type("UNMUTE").sessionId(sessionId).userId(userId).build());
    return muted;
  }

  @Override
  public List<LiveBoardSongDto> addSong(Long sessionId, LiveBoardSongDto dto) {
    List<LiveBoardSongDto> songs = serviceRecordClient.addBoardSong(sessionId, dto).getData();
    boardPushService.sendSongs(sessionId, songs);
    return songs;
  }

  @Override
  public List<LiveBoardSongDto> toggleSong(Long sessionId, Long songId, String action) {
    List<LiveBoardSongDto> songs = serviceRecordClient.toggleBoardSong(songId, action).getData();
    boardPushService.sendSongs(sessionId, songs);
    return songs;
  }

  @Override
  public List<LiveBoardSongDto> deleteSong(Long sessionId, Long songId) {
    List<LiveBoardSongDto> songs = serviceRecordClient.deleteBoardSong(songId).getData();
    boardPushService.sendSongs(sessionId, songs);
    return songs;
  }

  @Override
  public List<LiveBoardSongDto> getSongs(Long sessionId) {
    return serviceRecordClient.getBoardSongs(sessionId).getData();
  }

  @Override
  public List<LiveBoardSongDto> updateSong(Long sessionId, Long songId, String title, String note) {
    LiveBoardSongDto req = new LiveBoardSongDto();
    req.setTitle(title);
    req.setNote(note);
    List<LiveBoardSongDto> songs = serviceRecordClient.updateBoardSong(songId, req).getData();
    boardPushService.sendSongs(sessionId, songs);
    return songs;
  }

  @Override
  public List<LiveBoardSongDto> reorderSongs(Long sessionId, List<Long> songIds) {
    List<LiveBoardSongDto> songs = serviceRecordClient.reorderBoardSongs(sessionId, songIds).getData();
    boardPushService.sendSongs(sessionId, songs);
    return songs;
  }

  @Override
  public List<LiveBoardSongDto> copySongs(Long sessionId, Long sourceSessionId) {
    List<LiveBoardSongDto> songs = serviceRecordClient.copyBoardSongs(sessionId, sourceSessionId).getData();
    boardPushService.sendSongs(sessionId, songs);
    return songs;
  }

  @Override
  public int presence(Long sessionId, String userId) {
    if (userId == null || userId.isEmpty()) {
      return countPresence(sessionId);
    }
    String key = presenceKey(sessionId);
    redisTemplate.opsForZSet().add(key, userId, System.currentTimeMillis());
    int count = countPresence(sessionId);
    boardPushService.sendEvent(sessionId, LiveBoardEventDto.builder()
        .type("PRESENCE").sessionId(sessionId).presence(count).build());
    return count;
  }

  private int countPresence(Long sessionId) {
    String key = presenceKey(sessionId);
    long cutoff = System.currentTimeMillis() - PRESENCE_WINDOW_MS;
    redisTemplate.opsForZSet().removeRangeByScore(key, 0, cutoff - 1);
    Long size = redisTemplate.opsForZSet().zCard(key);
    return size == null ? 0 : size.intValue();
  }
}
