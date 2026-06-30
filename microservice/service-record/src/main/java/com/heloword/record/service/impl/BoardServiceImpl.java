package com.heloword.record.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.heloword.common.entity.board.LiveBoardMessageEntity;
import com.heloword.common.entity.board.LiveBoardMuteEntity;
import com.heloword.common.entity.board.LiveBoardSessionEntity;
import com.heloword.common.entity.board.LiveBoardSongEntity;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardMuteDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSnapshotDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;
import com.heloword.common.repo.board.LiveBoardMessageRepository;
import com.heloword.common.repo.board.LiveBoardMuteRepository;
import com.heloword.common.repo.board.LiveBoardSessionRepository;
import com.heloword.common.repo.board.LiveBoardSongRepository;
import com.heloword.record.service.BoardService;

@Log4j2
@Service
public class BoardServiceImpl implements BoardService {

  public static final String STATE_ACTIVE = "ACTIVE";
  public static final String STATE_ENDED = "ENDED";

  @Autowired
  private LiveBoardSessionRepository sessionRepo;
  @Autowired
  private LiveBoardMessageRepository messageRepo;
  @Autowired
  private LiveBoardSongRepository songRepo;
  @Autowired
  private LiveBoardMuteRepository muteRepo;

  @Override
  @Transactional
  public LiveBoardSessionDto createSession(LiveBoardSessionDto dto) {
    endAllActive();
    LiveBoardSessionEntity e = new LiveBoardSessionEntity();
    e.setName(dto.getName());
    e.setBoardState(STATE_ACTIVE);
    e.setCreatedByUserId(dto.getCreatedByUserId());
    e.setCreatedByName(dto.getCreatedByName());
    return LiveBoardSessionDto.fromEntity(sessionRepo.save(e));
  }

  @Override
  public List<LiveBoardSessionDto> getSessions() {
    return sessionRepo.findAllByOrderByIdDesc().stream()
        .map(LiveBoardSessionDto::fromEntity)
        .collect(Collectors.toList());
  }

  @Override
  public LiveBoardSessionDto getActiveSession() {
    return sessionRepo.findFirstByBoardStateOrderByIdDesc(STATE_ACTIVE)
        .map(LiveBoardSessionDto::fromEntity)
        .orElse(null);
  }

  @Override
  public LiveBoardSnapshotDto getSnapshot(Long sessionId) {
    LiveBoardSessionEntity session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    LiveBoardSnapshotDto snapshot = new LiveBoardSnapshotDto();
    snapshot.setSession(LiveBoardSessionDto.fromEntity(session));
    snapshot.setMessages(messageRepo.findAllBySessionIdAndDeletedFalseOrderByIdAsc(sessionId).stream()
        .map(LiveBoardMessageDto::fromEntity).collect(Collectors.toList()));
    snapshot.setSongs(songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sessionId).stream()
        .map(LiveBoardSongDto::fromEntity).collect(Collectors.toList()));
    snapshot.setMutedUserIds(muteRepo.findAllBySessionId(sessionId).stream()
        .map(LiveBoardMuteEntity::getUserId).collect(Collectors.toList()));
    return snapshot;
  }

  @Override
  @Transactional
  public LiveBoardSessionDto endSession(Long sessionId) {
    LiveBoardSessionEntity session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    session.setBoardState(STATE_ENDED);
    session.setEndedDate(new Date());
    return LiveBoardSessionDto.fromEntity(sessionRepo.save(session));
  }

  @Override
  @Transactional
  public LiveBoardSessionDto restartSession(Long sessionId) {
    LiveBoardSessionEntity session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    // Ensure single active session.
    endAllActive();
    session.setBoardState(STATE_ACTIVE);
    session.setEndedDate(null);
    return LiveBoardSessionDto.fromEntity(sessionRepo.save(session));
  }

  @Override
  @Transactional
  public LiveBoardMessageDto addMessage(Long sessionId, LiveBoardMessageDto dto) {
    LiveBoardMessageEntity e = new LiveBoardMessageEntity();
    e.setSessionId(sessionId);
    e.setAuthorUserId(dto.getAuthorUserId());
    e.setAuthorName(dto.getAuthorName());
    e.setContent(dto.getContent());
    e.setOfficial(Boolean.TRUE.equals(dto.getOfficial()));
    e.setDeleted(false);
    return LiveBoardMessageDto.fromEntity(messageRepo.save(e));
  }

  @Override
  @Transactional
  public LiveBoardMessageDto deleteMessage(Long messageId) {
    LiveBoardMessageEntity e = messageRepo.findById(messageId)
        .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
    e.setDeleted(true);
    return LiveBoardMessageDto.fromEntity(messageRepo.save(e));
  }

  @Override
  public boolean isMuted(Long sessionId, String userId) {
    return muteRepo.existsBySessionIdAndUserId(sessionId, userId);
  }

  @Override
  @Transactional
  public List<String> mute(Long sessionId, LiveBoardMuteDto dto) {
    if (!muteRepo.existsBySessionIdAndUserId(sessionId, dto.getUserId())) {
      LiveBoardMuteEntity e = new LiveBoardMuteEntity();
      e.setSessionId(sessionId);
      e.setUserId(dto.getUserId());
      e.setMutedName(dto.getMutedName());
      muteRepo.save(e);
    }
    return mutedUserIds(sessionId);
  }

  @Override
  @Transactional
  public List<String> unmute(Long sessionId, String userId) {
    muteRepo.deleteBySessionIdAndUserId(sessionId, userId);
    return mutedUserIds(sessionId);
  }

  @Override
  public List<LiveBoardSongDto> getSongs(Long sessionId) {
    return songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sessionId).stream()
        .map(LiveBoardSongDto::fromEntity).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public List<LiveBoardSongDto> addSong(Long sessionId, LiveBoardSongDto dto) {
    LiveBoardSongEntity e = new LiveBoardSongEntity();
    e.setSessionId(sessionId);
    e.setTitle(dto.getTitle());
    e.setSung(false);
    e.setRequestCount(0);
    int nextOrder = songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sessionId).size();
    e.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : nextOrder);
    songRepo.save(e);
    return getSongs(sessionId);
  }

  @Override
  @Transactional
  public List<LiveBoardSongDto> toggleSong(Long songId, String action) {
    LiveBoardSongEntity e = songRepo.findById(songId)
        .orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId));
    if ("request".equals(action)) {
      e.setRequestCount((e.getRequestCount() == null ? 0 : e.getRequestCount()) + 1);
    } else { // "sung" (default)
      e.setSung(!Boolean.TRUE.equals(e.getSung()));
    }
    songRepo.save(e);
    return getSongs(e.getSessionId());
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  private void endAllActive() {
    sessionRepo.findFirstByBoardStateOrderByIdDesc(STATE_ACTIVE).ifPresent(active -> {
      active.setBoardState(STATE_ENDED);
      active.setEndedDate(new Date());
      sessionRepo.save(active);
    });
  }

  private List<String> mutedUserIds(Long sessionId) {
    return muteRepo.findAllBySessionId(sessionId).stream()
        .map(LiveBoardMuteEntity::getUserId).collect(Collectors.toList());
  }
}
