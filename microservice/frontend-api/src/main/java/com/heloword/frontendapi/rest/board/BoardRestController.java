package com.heloword.frontendapi.rest.board;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.model.dto.board.LiveBoardMessageDto;
import com.heloword.common.model.dto.board.LiveBoardMuteDto;
import com.heloword.common.model.dto.board.LiveBoardSessionDto;
import com.heloword.common.model.dto.board.LiveBoardSnapshotDto;
import com.heloword.common.model.dto.board.LiveBoardSongDto;
import com.heloword.common.type.ResponseCode;
import com.heloword.frontendapi.service.board.BoardFrontendService;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

/**
 * Live busking board. Identity is UUID-only — username/email is never read,
 * stored, returned, or broadcast.
 */
@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/board")
@AllArgsConstructor
public class BoardRestController extends AbstractBaseFrontendRestController {

  private final BoardFrontendService boardFrontendService;

  // ── Public (MEMBER + UNREGISTERED_MEMBER, enforced by the /fe/** filter) ────

  @GetMapping("/active")
  public HeloResponse<?> getActive() {
    return HeloResponse.successWithData(boardFrontendService.getActiveSession());
  }

  @GetMapping("/sessions/{id}")
  public HeloResponse<?> getSnapshot(@PathVariable Long id, @RequestParam(required = false) String userId) {
    // Member UUID wins; guests pass their guest UUID as the query param.
    LiveBoardSnapshotDto snapshot = boardFrontendService.getSnapshot(id, resolveUserId(userId));
    if (snapshot != null && !isAdmin()) {
      snapshot.setSongs(LiveBoardSongDto.withoutNotes(snapshot.getSongs()));
    }
    return HeloResponse.successWithData(snapshot);
  }

  @PostMapping("/sessions/{id}/messages/{messageId}/like")
  public HeloResponse<?> toggleLike(@PathVariable Long id, @PathVariable Long messageId,
      @RequestBody(required = false) LiveBoardMessageDto body) {
    String userId = resolveUserId(body == null ? null : body.getAuthorUserId());
    if (userId == null || userId.isEmpty()) {
      return fail("Missing user.");
    }
    return HeloResponse.successWithData(boardFrontendService.toggleLike(id, messageId, userId));
  }

  @PostMapping("/sessions/{id}/messages")
  public HeloResponse<?> postMessage(@PathVariable Long id, @RequestBody LiveBoardMessageDto body) {
    String content = body.getContent() == null ? "" : body.getContent().trim();
    if (content.isEmpty()) {
      return fail("Message cannot be empty.");
    }
    if (content.length() > 1000) {
      content = content.substring(0, 1000);
    }
    LiveBoardMessageDto dto = new LiveBoardMessageDto();
    dto.setContent(content);
    applyIdentity(dto, body);
    return HeloResponse.successWithData(boardFrontendService.postMessage(id, dto));
  }

  @PostMapping("/sessions/{id}/presence")
  public HeloResponse<?> presence(@PathVariable Long id, @RequestBody LiveBoardMessageDto body) {
    String userId = resolveUserId(body.getAuthorUserId());
    return HeloResponse.successWithData(boardFrontendService.presence(id, userId));
  }

  @PostMapping("/sessions/{id}/songs/{songId}/toggle")
  public HeloResponse<?> toggleSong(@PathVariable Long id, @PathVariable Long songId,
      @RequestParam(defaultValue = "sung") String action) {
    // Only "request" is open to the audience; "sung"/"performing" are host-only.
    if (!"request".equals(action) && !isAdmin()) {
      return fail(ResponseCode.INSUFFICIENT_AUTHORITY);
    }
    return HeloResponse.successWithData(songsFor(boardFrontendService.toggleSong(id, songId, action)));
  }

  // ── Admin only ──────────────────────────────────────────────────────────────

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions")
  public HeloResponse<?> createSession(@RequestBody LiveBoardSessionDto body) {
    UserDto admin = requireAdmin();
    String name = body.getName() == null ? "" : body.getName().trim();
    if (name.isEmpty()) {
      return fail("Session name is required.");
    }
    return HeloResponse.successWithData(
        boardFrontendService.createSession(name, admin.getUuid(), displayName(admin)));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/sessions")
  public HeloResponse<?> getSessions() {
    return HeloResponse.successWithData(boardFrontendService.getSessions());
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/end")
  public HeloResponse<?> endSession(@PathVariable Long id) {
    return HeloResponse.successWithData(boardFrontendService.endSession(id));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/restart")
  public HeloResponse<?> restartSession(@PathVariable Long id) {
    return HeloResponse.successWithData(boardFrontendService.restartSession(id));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/official")
  public HeloResponse<?> postOfficial(@PathVariable Long id, @RequestBody LiveBoardMessageDto body) {
    UserDto admin = requireAdmin();
    String content = body.getContent() == null ? "" : body.getContent().trim();
    if (content.isEmpty()) {
      return fail("Message cannot be empty.");
    }
    LiveBoardMessageDto dto = new LiveBoardMessageDto();
    dto.setContent(content);
    dto.setAuthorUserId(admin.getUuid());
    dto.setAuthorName(displayName(admin));
    return HeloResponse.successWithData(boardFrontendService.postOfficial(id, dto));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/messages/{messageId}")
  public HeloResponse<?> deleteMessage(@PathVariable Long messageId) {
    return HeloResponse.successWithData(boardFrontendService.deleteMessage(messageId));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/mute")
  public HeloResponse<?> mute(@PathVariable Long id, @RequestBody LiveBoardMuteDto body) {
    if (body.getUserId() == null || body.getUserId().isEmpty()) {
      return fail("Missing user.");
    }
    return HeloResponse.successWithData(boardFrontendService.mute(id, body));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/sessions/{id}/mute/{userId}")
  public HeloResponse<?> unmute(@PathVariable Long id, @PathVariable String userId) {
    return HeloResponse.successWithData(boardFrontendService.unmute(id, userId));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/songs")
  public HeloResponse<?> addSong(@PathVariable Long id, @RequestBody LiveBoardSongDto body) {
    if (body.getTitle() == null || body.getTitle().trim().isEmpty()) {
      return fail("Song title is required.");
    }
    body.setTitle(body.getTitle().trim());
    return HeloResponse.successWithData(boardFrontendService.addSong(id, body));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @DeleteMapping("/sessions/{id}/songs/{songId}")
  public HeloResponse<?> deleteSong(@PathVariable Long id, @PathVariable Long songId) {
    return HeloResponse.successWithData(boardFrontendService.deleteSong(id, songId));
  }

  /** Full setlist with host-private notes — used by the admin setlist editor. */
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/sessions/{id}/songs")
  public HeloResponse<?> getSongs(@PathVariable Long id) {
    return HeloResponse.successWithData(boardFrontendService.getSongs(id));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/songs/{songId}/note")
  public HeloResponse<?> updateSongNote(@PathVariable Long id, @PathVariable Long songId,
      @RequestBody LiveBoardSongDto body) {
    String note = body.getNote() == null ? "" : body.getNote().trim();
    if (note.length() > 500) {
      note = note.substring(0, 500);
    }
    return HeloResponse.successWithData(boardFrontendService.updateSongNote(id, songId, note));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/songs/reorder")
  public HeloResponse<?> reorderSongs(@PathVariable Long id, @RequestBody List<Long> songIds) {
    if (songIds == null || songIds.isEmpty()) {
      return fail("No songs to reorder.");
    }
    return HeloResponse.successWithData(boardFrontendService.reorderSongs(id, songIds));
  }

  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping("/sessions/{id}/songs/copy")
  public HeloResponse<?> copySongs(@PathVariable Long id, @RequestParam Long sourceSessionId) {
    if (sourceSessionId == null || sourceSessionId.equals(id)) {
      return fail("Pick a different board to copy from.");
    }
    return HeloResponse.successWithData(boardFrontendService.copySongs(id, sourceSessionId));
  }

  /**
   * Songs as the caller is allowed to see them. The host-private note is dropped
   * for everyone but an ADMIN — hiding it client-side would still ship it in the
   * JSON that any audience member can read.
   */
  private List<LiveBoardSongDto> songsFor(List<LiveBoardSongDto> songs) {
    return isAdmin() ? songs : LiveBoardSongDto.withoutNotes(songs);
  }

  // ── identity helpers (UUID-only) ────────────────────────────────────────────

  /** True if the current request is authenticated as an ADMIN. */
  private boolean isAdmin() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null && auth.getAuthorities().stream()
        .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
  }

  /**
   * Resolve the author identity. For a logged-in member, the UUID + nickname from the
   * session are authoritative (request-supplied values ignored). For a guest, the
   * request-supplied guest UUID + typed name are used. Never falls back to username/email.
   */
  private void applyIdentity(LiveBoardMessageDto dto, LiveBoardMessageDto body) {
    Optional<UserDto> current = getUser();
    if (current.isPresent()) {
      UserDto user = current.get();
      if (user.getUuid() == null) {
        throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR, "Missing user id.");
      }
      dto.setAuthorUserId(user.getUuid());
      dto.setAuthorName(displayName(user));
    } else {
      String guestId = body.getAuthorUserId();
      if (guestId == null || guestId.trim().isEmpty()) {
        throw HeloServiceException.of(ResponseCode.INVALID_REQUEST, "Missing guest id.");
      }
      dto.setAuthorUserId(guestId.trim());
      String name = body.getAuthorName() == null ? "" : body.getAuthorName().trim();
      dto.setAuthorName(name.isEmpty() ? "Guest" : name);
    }
  }

  /** Member UUID if logged in, else the request-supplied guest UUID. */
  private String resolveUserId(String guestId) {
    return getUser().map(UserDto::getUuid).orElse(guestId);
  }

  private UserDto requireAdmin() {
    return getUser().orElseThrow(() -> HeloServiceException.of(ResponseCode.INSUFFICIENT_AUTHORITY));
  }

  /** Display name = nickname, falling back to a neutral label. Never username/email. */
  private static String displayName(UserDto user) {
    return user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : "Host";
  }
}
