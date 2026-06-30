package com.heloword.frontendapi.service.board;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.heloword.common.model.dto.board.LiveBoardEventDto;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;

/**
 * Broadcasts live-board changes over STOMP. All payloads are UUID-only DTOs
 * (no username/email).
 */
@Log4j2
@Service
@AllArgsConstructor
public class BoardPushService {

  private final SimpMessagingTemplate messagingTemplate;

  /** New or official message → everyone viewing the session. */
  public void sendMessage(Long sessionId, LiveBoardMessageDto message) {
    messagingTemplate.convertAndSend("/topic/board/" + sessionId + "/messages", message);
  }

  /** Control event (delete/mute/unmute/ended/restarted/presence). */
  public void sendEvent(Long sessionId, LiveBoardEventDto event) {
    messagingTemplate.convertAndSend("/topic/board/" + sessionId + "/events", event);
  }

  /** Updated setlist. */
  public void sendSongs(Long sessionId, List<LiveBoardSongDto> songs) {
    messagingTemplate.convertAndSend("/topic/board/" + sessionId + "/songs", songs);
  }

  /**
   * Global live/ended status used by the home banner + entry pop-up.
   * Carries the session with its boardState (ACTIVE on start/restart, ENDED on end).
   */
  public void broadcastActive(LiveBoardSessionDto session) {
    messagingTemplate.convertAndSend("/topic/board/active", session);
  }
}
