package com.heloword.frontendapi.rest.social;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

  // ── Online presence (no auth restriction — both MEMBER + UNREGISTERED_MEMBER) ──

  @PostMapping("/heartbeat")
  public HeloResponse<?> heartbeat(@RequestBody HeartbeatRequest request) {
    socialFrontendService.heartbeat(request);
    return HeloResponse.successWithoutData();
  }

  @DeleteMapping("/heartbeat/{userId}")
  public HeloResponse<?> removeHeartbeat(@PathVariable String userId) {
    socialFrontendService.removeHeartbeat(userId);
    return HeloResponse.successWithoutData();
  }

  @GetMapping("/online-users")
  public HeloResponse<?> getOnlineUsers() {
    return HeloResponse.successWithData(socialFrontendService.getOnlineUsers());
  }

  // ── Chat (no auth restriction) ────────────────────────────────────────────

  @PostMapping("/messages")
  public HeloResponse<?> sendMessage(@RequestBody ChatMessageDto dto) {
    return HeloResponse.successWithData(socialFrontendService.sendMessage(dto));
  }

  @GetMapping("/messages/room/{roomId}")
  public HeloResponse<?> getMessages(@PathVariable String roomId,
      @RequestParam(required = false) Long since) {
    return HeloResponse.successWithData(socialFrontendService.getMessages(roomId, since));
  }

  @PostMapping("/messages/read/{roomId}")
  public HeloResponse<?> markRoomRead(@PathVariable String roomId,
      @RequestParam String recipientUserId) {
    socialFrontendService.markRoomRead(recipientUserId, roomId);
    return HeloResponse.successWithoutData();
  }

  @GetMapping("/messages/unread")
  public HeloResponse<?> getUnreadCounts(@RequestParam String recipientUserId) {
    return HeloResponse.successWithData(socialFrontendService.getUnreadCounts(recipientUserId));
  }

  @GetMapping("/messages/rooms")
  public HeloResponse<?> getChatRooms(@RequestParam String userId) {
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
