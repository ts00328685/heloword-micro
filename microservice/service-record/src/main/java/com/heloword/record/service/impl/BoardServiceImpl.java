package com.heloword.record.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.heloword.common.entity.board.LiveBoardMessageEntity;
import com.heloword.common.entity.board.LiveBoardMessageLikeEntity;
import com.heloword.common.entity.board.LiveBoardMuteEntity;
import com.heloword.common.entity.board.LiveBoardSessionEntity;
import com.heloword.common.entity.board.LiveBoardSongEntity;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardMuteDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSnapshotDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;
import com.heloword.common.repo.board.LiveBoardMessageLikeRepository;
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
  @Autowired
  private LiveBoardMessageLikeRepository likeRepo;

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
  public LiveBoardSnapshotDto getSnapshot(Long sessionId, String userId) {
    LiveBoardSessionEntity session = sessionRepo.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

    List<LiveBoardMessageDto> messages = messageRepo.findAllBySessionIdAndDeletedFalseOrderByIdAsc(sessionId).stream()
        .map(LiveBoardMessageDto::fromEntity)
        .peek(m -> m.setLikeCount((int) likeRepo.countByMessageId(m.getId())))
        .collect(Collectors.toList());

    // Which of these messages the caller has liked.
    List<Long> messageIds = messages.stream().map(LiveBoardMessageDto::getId).collect(Collectors.toList());
    List<Long> likedIds;
    if (userId != null && !userId.isEmpty() && !messageIds.isEmpty()) {
      Set<Long> liked = likeRepo.findAllByUserIdAndMessageIdIn(userId, messageIds).stream()
          .map(LiveBoardMessageLikeEntity::getMessageId).collect(Collectors.toSet());
      messages.forEach(m -> m.setLiked(liked.contains(m.getId())));
      likedIds = new java.util.ArrayList<>(liked);
    } else {
      likedIds = new java.util.ArrayList<>();
    }

    LiveBoardSnapshotDto snapshot = new LiveBoardSnapshotDto();
    snapshot.setSession(LiveBoardSessionDto.fromEntity(session));
    snapshot.setMessages(messages);
    snapshot.setSongs(songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sessionId).stream()
        .map(LiveBoardSongDto::fromEntity).collect(Collectors.toList()));
    snapshot.setMutedUserIds(muteRepo.findAllBySessionId(sessionId).stream()
        .map(LiveBoardMuteEntity::getUserId).collect(Collectors.toList()));
    snapshot.setLikedMessageIds(likedIds);
    return snapshot;
  }

  @Override
  @Transactional
  public LiveBoardMessageDto toggleLike(Long messageId, String userId) {
    LiveBoardMessageEntity message = messageRepo.findById(messageId)
        .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));
    boolean liked;
    if (likeRepo.existsByMessageIdAndUserId(messageId, userId)) {
      likeRepo.deleteByMessageIdAndUserId(messageId, userId);
      liked = false;
    } else {
      LiveBoardMessageLikeEntity like = new LiveBoardMessageLikeEntity();
      like.setMessageId(messageId);
      like.setUserId(userId);
      likeRepo.save(like);
      liked = true;
    }
    LiveBoardMessageDto dto = LiveBoardMessageDto.fromEntity(message);
    dto.setLikeCount((int) likeRepo.countByMessageId(messageId));
    dto.setLiked(liked);
    return dto;
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
    e.setPerforming(false);
    e.setRequestCount(0);
    int nextOrder = songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sessionId).size();
    e.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : nextOrder);
    e.setNote(dto.getNote());
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
    } else if ("performing".equals(action)) {
      boolean next = !Boolean.TRUE.equals(e.getPerforming());
      if (next) {
        // Only one song performs at a time — clear the rest in this session.
        songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(e.getSessionId()).forEach(other -> {
          if (!other.getId().equals(songId) && Boolean.TRUE.equals(other.getPerforming())) {
            other.setPerforming(false);
            songRepo.save(other);
          }
        });
      }
      e.setPerforming(next);
    } else { // "sung" (default)
      e.setSung(!Boolean.TRUE.equals(e.getSung()));
    }
    songRepo.save(e);
    return getSongs(e.getSessionId());
  }

  @Override
  @Transactional
  public List<LiveBoardSongDto> deleteSong(Long songId) {
    LiveBoardSongEntity e = songRepo.findById(songId)
        .orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId));
    Long sessionId = e.getSessionId();
    songRepo.deleteById(songId);
    return getSongs(sessionId);
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public List<LiveBoardSongDto> updateSong(Long songId, LiveBoardSongDto dto) {
    LiveBoardSongEntity e = songRepo.findById(songId)
        .orElseThrow(() -> new IllegalArgumentException("Song not found: " + songId));
    // A blank title would leave an unidentifiable row in the setlist, so an empty
    // one is treated as "not editing the title" rather than as a rename to "".
    String title = dto.getTitle() == null ? "" : dto.getTitle().trim();
    if (!title.isEmpty()) {
      e.setTitle(title);
    }
    String note = dto.getNote() == null ? "" : dto.getNote().trim();
    e.setNote(note.isEmpty() ? null : note);
    songRepo.save(e);
    return getSongs(e.getSessionId());
  }

  @Override
  @Transactional
  public List<LiveBoardSongDto> reorderSongs(Long sessionId, List<Long> songIds) {
    List<LiveBoardSongEntity> songs = songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sessionId);
    List<Long> order = songIds == null ? new ArrayList<>() : songIds;
    // A song missing from the request (added by a concurrent admin tab, say) must
    // not collapse onto sortOrder 0 — park those after everything explicitly
    // ranked, keeping the order they already had.
    int unranked = order.size();
    for (LiveBoardSongEntity song : songs) {
      int rank = order.indexOf(song.getId());
      song.setSortOrder(rank >= 0 ? rank : unranked++);
    }
    songRepo.saveAll(songs);
    return getSongs(sessionId);
  }

  @Override
  @Transactional
  public List<LiveBoardSongDto> copySongs(Long sessionId, Long sourceSessionId) {
    if (sessionId.equals(sourceSessionId)) {
      throw new IllegalArgumentException("Cannot copy a setlist onto itself.");
    }
    // Fresh rows, not references: the new board starts unsung and unrequested, so
    // last night's play counts never bleed into tonight's set.
    int nextOrder = songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sessionId).size();
    List<LiveBoardSongEntity> copies = new ArrayList<>();
    for (LiveBoardSongEntity source : songRepo.findAllBySessionIdOrderBySortOrderAscIdAsc(sourceSessionId)) {
      LiveBoardSongEntity copy = new LiveBoardSongEntity();
      copy.setSessionId(sessionId);
      copy.setTitle(source.getTitle());
      copy.setNote(source.getNote());
      copy.setSung(false);
      copy.setPerforming(false);
      copy.setRequestCount(0);
      copy.setSortOrder(nextOrder++);
      copies.add(copy);
    }
    songRepo.saveAll(copies);
    return getSongs(sessionId);
  }

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
