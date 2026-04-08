package com.heloword.frontendapi.rest.social;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.model.dto.ChatMessageDto;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;
import com.heloword.frontendapi.model.request.HeartbeatRequest;
import com.heloword.frontendapi.service.social.SocialFrontendService;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/social")
@AllArgsConstructor
public class SocialRestController extends AbstractBaseFrontendRestController {

  private SocialFrontendService socialFrontendService;

  // ── Online presence (open to MEMBER + UNREGISTERED_MEMBER) ──────────────────

  @PostMapping("/heartbeat")
  public HeloResponse<?> heartbeat(@RequestBody HeartbeatRequest request) {
    Optional<UserDto> currentUser = getUser();
    if (currentUser.isPresent()) {
      UserDto user = currentUser.get();
      // Use UUID if available; fall back to username until SQL migration is complete
      String userId = user.getUuid() != null ? user.getUuid() : user.getUsername();
      request.setUserId(userId);
      request.setDisplayName(user.getNickname() != null ? user.getNickname() : user.getUsername());
      request.setIsGuest(false);
    }
    // Guest: allow request-supplied values as-is (no server-side identity to verify)
    socialFrontendService.heartbeat(request);
    return HeloResponse.successWithoutData();
  }

  @DeleteMapping("/heartbeat/{userId}")
  public HeloResponse<?> removeHeartbeat(@PathVariable String userId) {
    Optional<UserDto> currentUser = getUser();
    if (currentUser.isPresent()) {
      String myId = currentUser.get().getUuid() != null ? currentUser.get().getUuid() : currentUser.get().getUsername();
      if (!myId.equals(userId)) {
        return fail("Forbidden");
      }
    }
    socialFrontendService.removeHeartbeat(userId);
    return HeloResponse.successWithoutData();
  }

  @GetMapping("/online-users")
  public HeloResponse<?> getOnlineUsers() {
    return HeloResponse.successWithData(socialFrontendService.getOnlineUsers());
  }

  // ── Chat (MEMBER only) ────────────────────────────────────────────────────

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/messages")
  public HeloResponse<?> sendMessage(@RequestBody ChatMessageDto dto) {
    UserDto user = getUser().get();
    String userId = user.getUuid() != null ? user.getUuid() : user.getUsername();
    dto.setSenderUserId(userId);
    dto.setSenderDisplayName(user.getNickname() != null ? user.getNickname() : user.getUsername());
    return HeloResponse.successWithData(socialFrontendService.sendMessage(dto));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @GetMapping("/messages/room/{roomId}")
  public HeloResponse<?> getMessages(@PathVariable String roomId,
      @RequestParam(required = false) Long since) {
    return HeloResponse.successWithData(socialFrontendService.getMessages(roomId, since));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/messages/read/{roomId}")
  public HeloResponse<?> markRoomRead(@PathVariable String roomId) {
    UserDto user = getUser().get();
    String userId = user.getUuid() != null ? user.getUuid() : user.getUsername();
    socialFrontendService.markRoomRead(userId, roomId);
    return HeloResponse.successWithoutData();
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @GetMapping("/messages/unread")
  public HeloResponse<?> getUnreadCounts() {
    UserDto user = getUser().get();
    String userId = user.getUuid() != null ? user.getUuid() : user.getUsername();
    return HeloResponse.successWithData(socialFrontendService.getUnreadCounts(userId));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @GetMapping("/messages/rooms")
  public HeloResponse<?> getChatRooms() {
    UserDto user = getUser().get();
    String userId = user.getUuid() != null ? user.getUuid() : user.getUsername();
    return HeloResponse.successWithData(socialFrontendService.getChatRooms(userId));
  }

  // ── Friends (MEMBER only) ─────────────────────────────────────────────────

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @GetMapping("/friends")
  public HeloResponse<?> getFriends() {
    return HeloResponse.successWithData(socialFrontendService.getFriends(getUser().get()));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/friends/request")
  public HeloResponse<?> sendFriendRequest(@RequestBody String addresseeUsername) {
    return HeloResponse.successWithData(socialFrontendService.sendFriendRequest(getUser().get(), addresseeUsername));
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/friends/accept/{id}")
  public HeloResponse<?> acceptFriendRequest(@PathVariable Long id) {
    socialFrontendService.acceptFriendRequest(getUser().get(), id);
    return HeloResponse.successWithoutData();
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/friends/reject/{id}")
  public HeloResponse<?> rejectFriendRequest(@PathVariable Long id) {
    socialFrontendService.rejectFriendRequest(getUser().get(), id);
    return HeloResponse.successWithoutData();
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @DeleteMapping("/friends/{id}")
  public HeloResponse<?> removeFriend(@PathVariable Long id) {
    socialFrontendService.removeFriend(getUser().get(), id);
    return HeloResponse.successWithoutData();
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PutMapping("/friends/{id}/nickname")
  public HeloResponse<?> updateFriendNickname(@PathVariable Long id, @RequestBody String nickname) {
    socialFrontendService.updateFriendNickname(getUser().get(), id, nickname);
    return HeloResponse.successWithoutData();
  }
}
