package com.heloword.record.service;

import java.util.List;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardMuteDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSnapshotDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;

public interface BoardService {

  LiveBoardSessionDto createSession(LiveBoardSessionDto dto);

  List<LiveBoardSessionDto> getSessions();

  LiveBoardSessionDto getActiveSession();

  LiveBoardSnapshotDto getSnapshot(Long sessionId);

  LiveBoardSessionDto endSession(Long sessionId);

  LiveBoardSessionDto restartSession(Long sessionId);

  LiveBoardMessageDto addMessage(Long sessionId, LiveBoardMessageDto dto);

  LiveBoardMessageDto deleteMessage(Long messageId);

  boolean isMuted(Long sessionId, String userId);

  List<String> mute(Long sessionId, LiveBoardMuteDto dto);

  List<String> unmute(Long sessionId, String userId);

  List<LiveBoardSongDto> getSongs(Long sessionId);

  List<LiveBoardSongDto> addSong(Long sessionId, LiveBoardSongDto dto);

  List<LiveBoardSongDto> toggleSong(Long songId, String action);
}
