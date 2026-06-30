package com.heloword.frontendapi.service.board;

import java.util.List;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardMuteDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSnapshotDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;

public interface BoardFrontendService {

  LiveBoardSessionDto getActiveSession();

  List<LiveBoardSessionDto> getSessions();

  LiveBoardSnapshotDto getSnapshot(Long sessionId);

  LiveBoardSessionDto createSession(String name, String creatorUuid, String creatorName);

  LiveBoardSessionDto endSession(Long sessionId);

  LiveBoardSessionDto restartSession(Long sessionId);

  /** Audience/member post. Enforces ACTIVE session + not-muted. */
  LiveBoardMessageDto postMessage(Long sessionId, LiveBoardMessageDto dto);

  /** Admin pinned message. */
  LiveBoardMessageDto postOfficial(Long sessionId, LiveBoardMessageDto dto);

  LiveBoardMessageDto deleteMessage(Long messageId);

  List<String> mute(Long sessionId, LiveBoardMuteDto dto);

  List<String> unmute(Long sessionId, String userId);

  List<LiveBoardSongDto> addSong(Long sessionId, LiveBoardSongDto dto);

  List<LiveBoardSongDto> toggleSong(Long sessionId, Long songId, String action);

  /** Record a viewer heartbeat; returns the current live viewer count. */
  int presence(Long sessionId, String userId);
}
