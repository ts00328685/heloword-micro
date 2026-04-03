package com.heloword.record.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.social.FriendEntity;
import com.heloword.common.model.dto.ChatMessageDto;
import com.heloword.common.model.dto.FriendDto;
import com.heloword.record.service.SocialService;

@Log4j2
@RestController
@RequestMapping("/social")
public class SocialRestController {

  @Autowired
  private SocialService socialService;

  // ── Friends ──────────────────────────────────────────────────────────────

  @GetMapping("/friends")
  public HeloResponse<?> getFriends(@RequestHeader String username) {
    List<FriendEntity> friends = socialService.getFriends(decodeHeader(username));
    List<FriendDto> dtos = friends.stream().map(this::toFriendDto).collect(Collectors.toList());
    return HeloResponse.successWithData(dtos);
  }

  @PostMapping("/friends/request")
  public HeloResponse<?> sendFriendRequest(@RequestHeader String username, @RequestHeader String addresseeUsername) {
    log.info("sendFriendRequest — raw-username=[{}] raw-addressee=[{}]", username, addresseeUsername);
    FriendEntity saved = socialService.sendFriendRequest(decodeHeader(username), decodeHeader(addresseeUsername));
    return HeloResponse.successWithData(toFriendDto(saved));
  }

  @PostMapping("/friends/accept/{id}")
  public HeloResponse<?> acceptFriendRequest(@RequestHeader String username, @PathVariable Long id) {
    FriendEntity accepted = socialService.acceptFriendRequest(decodeHeader(username), id);
    return HeloResponse.successWithData(toFriendDto(accepted));
  }

  @PostMapping("/friends/reject/{id}")
  public HeloResponse<?> rejectFriendRequest(@RequestHeader String username, @PathVariable Long id) {
    socialService.rejectFriendRequest(decodeHeader(username), id);
    return HeloResponse.successWithoutData();
  }

  @DeleteMapping("/friends/{id}")
  public HeloResponse<?> removeFriend(@RequestHeader String username, @PathVariable Long id) {
    socialService.removeFriend(decodeHeader(username), id);
    return HeloResponse.successWithoutData();
  }

  @PutMapping("/friends/{id}/nickname")
  public HeloResponse<?> updateFriendNickname(@RequestHeader String username, @PathVariable Long id, @RequestBody String nickname) {
    socialService.updateFriendNickname(decodeHeader(username), id, nickname);
    return HeloResponse.successWithoutData();
  }

  // ── Chat ─────────────────────────────────────────────────────────────────

  @PostMapping("/messages")
  public HeloResponse<?> sendMessage(@RequestBody ChatMessageDto chatMessageDto) {
    return HeloResponse.successWithData(
        ChatMessageDto.fromEntity(socialService.sendMessage(ChatMessageDto.toEntity(chatMessageDto)))
    );
  }

  @GetMapping("/messages/room/{roomId}")
  public HeloResponse<?> getMessages(@PathVariable String roomId,
      @RequestParam(required = false) Long since) {
    return HeloResponse.successWithData(
        socialService.getMessages(roomId, since).stream()
            .map(ChatMessageDto::fromEntity)
            .collect(Collectors.toList())
    );
  }

  @PostMapping("/messages/read/{roomId}")
  public HeloResponse<?> markRoomRead(@RequestHeader String recipientUserId, @PathVariable String roomId) {
    socialService.markRoomRead(decodeHeader(recipientUserId), roomId);
    return HeloResponse.successWithoutData();
  }

  @GetMapping("/messages/unread")
  public HeloResponse<?> getUnreadCounts(@RequestHeader String recipientUserId) {
    return HeloResponse.successWithData(socialService.getUnreadCounts(decodeHeader(recipientUserId)));
  }

  @GetMapping("/messages/rooms")
  public HeloResponse<?> getChatRooms(@RequestHeader String userId) {
    return HeloResponse.successWithData(
        socialService.getChatRooms(decodeHeader(userId)).stream()
            .map(ChatMessageDto::fromEntity)
            .collect(Collectors.toList())
    );
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  /**
   * Feign 10.x percent-encodes reserved characters (e.g. '@' → '%40') in
   * @RequestHeader values via RFC 6570 expansion.  Decode here so the raw
   * username reaches the DB layer unchanged.
   */
  private static String decodeHeader(String value) {
    if (value == null) return null;
    try {
      return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      return value;
    }
  }

  private FriendDto toFriendDto(FriendEntity entity) {
    FriendDto dto = new FriendDto();
    dto.setId(entity.getId());
    dto.setRequesterUsername(entity.getRequesterUsername());
    dto.setAddresseeUsername(entity.getAddresseeUsername());
    dto.setFriendStatus(entity.getFriendStatus());
    dto.setRequesterNickname(entity.getRequesterNickname());
    dto.setAddresseeNickname(entity.getAddresseeNickname());
    return dto;
  }
}
