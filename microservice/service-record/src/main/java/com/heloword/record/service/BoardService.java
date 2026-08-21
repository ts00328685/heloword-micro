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

  LiveBoardSnapshotDto getSnapshot(Long sessionId, String userId);

  LiveBoardSessionDto endSession(Long sessionId);

  LiveBoardSessionDto restartSession(Long sessionId);

  LiveBoardMessageDto addMessage(Long sessionId, LiveBoardMessageDto dto);

  LiveBoardMessageDto deleteMessage(Long messageId);

  LiveBoardMessageDto toggleLike(Long messageId, String userId);

  boolean isMuted(Long sessionId, String userId);

  List<String> mute(Long sessionId, LiveBoardMuteDto dto);

  List<String> unmute(Long sessionId, String userId);

  List<LiveBoardSongDto> getSongs(Long sessionId);

  List<LiveBoardSongDto> addSong(Long sessionId, LiveBoardSongDto dto);

  List<LiveBoardSongDto> toggleSong(Long songId, String action);

  List<LiveBoardSongDto> deleteSong(Long songId);

  /** Set the host-private note on a song. */
  List<LiveBoardSongDto> updateSongNote(Long songId, String note);

  /** Rewrite sortOrder to match the given id order; ids not in the list keep their place at the end. */
  List<LiveBoardSongDto> reorderSongs(Long sessionId, List<Long> songIds);

  /** Append copies of another session's setlist as fresh rows on this session. */
  List<LiveBoardSongDto> copySongs(Long sessionId, Long sourceSessionId);
}
