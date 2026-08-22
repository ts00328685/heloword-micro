package com.heloword.record.rest;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardMuteDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;
import com.heloword.record.service.BoardService;

@Log4j2
@RestController
@RequestMapping("/board")
public class BoardRestController {

  @Autowired
  private BoardService boardService;

  @PostMapping("/sessions")
  public HeloResponse<?> createSession(@RequestBody LiveBoardSessionDto dto) {
    return HeloResponse.successWithData(boardService.createSession(dto));
  }

  @GetMapping("/sessions")
  public HeloResponse<?> getSessions() {
    return HeloResponse.successWithData(boardService.getSessions());
  }

  @GetMapping("/sessions/active")
  public HeloResponse<?> getActiveSession() {
    return HeloResponse.successWithData(boardService.getActiveSession());
  }

  @GetMapping("/sessions/{id}")
  public HeloResponse<?> getSnapshot(@PathVariable Long id, @RequestParam(required = false) String userId) {
    return HeloResponse.successWithData(boardService.getSnapshot(id, userId));
  }

  @PostMapping("/sessions/{id}/end")
  public HeloResponse<?> endSession(@PathVariable Long id) {
    return HeloResponse.successWithData(boardService.endSession(id));
  }

  @PostMapping("/sessions/{id}/restart")
  public HeloResponse<?> restartSession(@PathVariable Long id) {
    return HeloResponse.successWithData(boardService.restartSession(id));
  }

  @PostMapping("/sessions/{id}/messages")
  public HeloResponse<?> addMessage(@PathVariable Long id, @RequestBody LiveBoardMessageDto dto) {
    return HeloResponse.successWithData(boardService.addMessage(id, dto));
  }

  @DeleteMapping("/messages/{messageId}")
  public HeloResponse<?> deleteMessage(@PathVariable Long messageId) {
    return HeloResponse.successWithData(boardService.deleteMessage(messageId));
  }

  @PostMapping("/messages/{messageId}/like")
  public HeloResponse<?> toggleLike(@PathVariable Long messageId, @RequestParam String userId) {
    return HeloResponse.successWithData(boardService.toggleLike(messageId, userId));
  }

  @GetMapping("/sessions/{id}/muted/{userId}")
  public HeloResponse<?> isMuted(@PathVariable Long id, @PathVariable String userId) {
    return HeloResponse.successWithData(boardService.isMuted(id, userId));
  }

  @PostMapping("/sessions/{id}/mute")
  public HeloResponse<?> mute(@PathVariable Long id, @RequestBody LiveBoardMuteDto dto) {
    return HeloResponse.successWithData(boardService.mute(id, dto));
  }

  @DeleteMapping("/sessions/{id}/mute/{userId}")
  public HeloResponse<?> unmute(@PathVariable Long id, @PathVariable String userId) {
    return HeloResponse.successWithData(boardService.unmute(id, userId));
  }

  @GetMapping("/sessions/{id}/songs")
  public HeloResponse<?> getSongs(@PathVariable Long id) {
    return HeloResponse.successWithData(boardService.getSongs(id));
  }

  @PostMapping("/sessions/{id}/songs")
  public HeloResponse<?> addSong(@PathVariable Long id, @RequestBody LiveBoardSongDto dto) {
    return HeloResponse.successWithData(boardService.addSong(id, dto));
  }

  @PostMapping("/songs/{songId}/toggle")
  public HeloResponse<?> toggleSong(@PathVariable Long songId, @RequestParam String action) {
    return HeloResponse.successWithData(boardService.toggleSong(songId, action));
  }

  @DeleteMapping("/songs/{songId}")
  public HeloResponse<?> deleteSong(@PathVariable Long songId) {
    return HeloResponse.successWithData(boardService.deleteSong(songId));
  }

  @PostMapping("/songs/{songId}")
  public HeloResponse<?> updateSong(@PathVariable Long songId, @RequestBody LiveBoardSongDto dto) {
    return HeloResponse.successWithData(boardService.updateSong(songId, dto));
  }

  @PostMapping("/sessions/{id}/songs/reorder")
  public HeloResponse<?> reorderSongs(@PathVariable Long id, @RequestBody List<Long> songIds) {
    return HeloResponse.successWithData(boardService.reorderSongs(id, songIds));
  }

  @PostMapping("/sessions/{id}/songs/copy")
  public HeloResponse<?> copySongs(@PathVariable Long id, @RequestParam Long sourceSessionId) {
    return HeloResponse.successWithData(boardService.copySongs(id, sourceSessionId));
  }
}
