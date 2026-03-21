package com.heloword.frontendapi.service.social.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.ChatMessageDto;
import com.heloword.common.model.dto.FriendDto;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.request.HeartbeatRequest;
import com.heloword.frontendapi.model.response.FriendResponseDto;
import com.heloword.frontendapi.model.response.OnlineUserDto;
import com.heloword.frontendapi.service.social.OnlineSseService;
import com.heloword.frontendapi.service.social.SocialFrontendService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Log4j2
@Service
@AllArgsConstructor
public class SocialFrontendServiceImpl implements SocialFrontendService {

  private static final String ONLINE_KEY = "social:online";
  /** Users are considered online if they heartbeated within 90 seconds */
  private static final long ONLINE_WINDOW_MS = 90_000L;

  private RedisTemplate<String, Object> redisTemplate;
  private ServiceRecordClient serviceRecordClient;
  private OnlineSseService onlineSseService;

  @Override
  public void heartbeat(HeartbeatRequest request) {
    double score = System.currentTimeMillis();
    String member = request.getUserId() + "|" + request.getDisplayName() + "|" + (Boolean.TRUE.equals(request.getIsGuest()) ? "1" : "0");
    redisTemplate.opsForZSet().add(ONLINE_KEY, member, score);
    // Push updated list to all SSE subscribers immediately
    onlineSseService.broadcastOnlineUsers(getOnlineUsers());
  }

  @Override
  public void removeHeartbeat(String userId) {
    // Remove any member whose first segment matches the userId
    long cutoff = System.currentTimeMillis() - ONLINE_WINDOW_MS;
    Set<Object> members = redisTemplate.opsForZSet().rangeByScore(ONLINE_KEY, cutoff, Double.MAX_VALUE);
    if (members != null) {
      members.stream()
          .filter(m -> ((String) m).startsWith(userId + "|"))
          .forEach(m -> redisTemplate.opsForZSet().remove(ONLINE_KEY, m));
    }
    onlineSseService.broadcastOnlineUsers(getOnlineUsers());
  }

  @Override
  public SseEmitter subscribeForUser(String userId) {
    SseEmitter emitter = onlineSseService.createEmitterForUser(userId);
    // Send current snapshot immediately so the client doesn't wait for the next heartbeat
    try {
      emitter.send(SseEmitter.event()
          .name("online-users")
          .data(getOnlineUsers()));
    } catch (Exception e) {
      log.warn("Failed to send initial online-users snapshot for {}: {}", userId, e.getMessage());
      emitter.completeWithError(e);
    }
    return emitter;
  }

  @Override
  public List<OnlineUserDto> getOnlineUsers() {
    long cutoff = System.currentTimeMillis() - ONLINE_WINDOW_MS;
    // Remove stale entries
    redisTemplate.opsForZSet().removeRangeByScore(ONLINE_KEY, 0, cutoff - 1);
    // Retrieve current members
    Set<Object> members = redisTemplate.opsForZSet().rangeByScore(ONLINE_KEY, cutoff, Double.MAX_VALUE);
    if (members == null) return new ArrayList<>();
    return members.stream()
        .map(m -> parseOnlineUser((String) m))
        .filter(u -> u != null)
        .collect(Collectors.toList());
  }

  private OnlineUserDto parseOnlineUser(String member) {
    try {
      String[] parts = member.split("\\|", 3);
      if (parts.length < 3) return null;
      return OnlineUserDto.builder()
          .userId(parts[0])
          .displayName(parts[1])
          .isGuest("1".equals(parts[2]))
          .build();
    } catch (Exception e) {
      log.warn("Failed to parse online user member: {}", member);
      return null;
    }
  }

  @Override
  public List<FriendResponseDto> getFriends(UserDto user) {
    List<FriendDto> friends = serviceRecordClient.getFriends(user.getUsername()).getData();
    if (friends == null) return new ArrayList<>();

    // Get online set to mark who's online
    Set<String> onlineIds = getOnlineUsers().stream()
        .map(OnlineUserDto::getUserId)
        .collect(Collectors.toSet());

    return friends.stream().map(f -> {
      boolean iAmRequester = user.getUsername().equals(f.getRequesterUsername());
      String otherUserId = iAmRequester ? f.getAddresseeUsername() : f.getRequesterUsername();
      String myNickname = iAmRequester ? f.getRequesterNickname() : f.getAddresseeNickname();

      String status;
      if ("ACCEPTED".equals(f.getFriendStatus())) {
        status = "ACCEPTED";
      } else if (iAmRequester) {
        status = "PENDING_SENT";
      } else {
        status = "PENDING_RECEIVED";
      }

      return FriendResponseDto.builder()
          .id(f.getId())
          .otherUserId(otherUserId)
          .displayName(myNickname != null ? myNickname : otherUserId)
          .myNickname(myNickname)
          .status(status)
          .isOnline(onlineIds.contains(otherUserId))
          .build();
    }).collect(Collectors.toList());
  }

  @Override
  public FriendDto sendFriendRequest(UserDto user, String addresseeUsername) {
    FriendDto saved = serviceRecordClient.sendFriendRequest(user.getUsername(), addresseeUsername).getData();
    // Notify the addressee in real-time so their badge updates immediately
    onlineSseService.sendToUser(
        addresseeUsername,
        SseEmitter.event().name("new-friend-request").data(user.getUsername()));
    return saved;
  }

  @Override
  public void acceptFriendRequest(UserDto user, Long id) {
    serviceRecordClient.acceptFriendRequest(user.getUsername(), id);
  }

  @Override
  public void rejectFriendRequest(UserDto user, Long id) {
    serviceRecordClient.rejectFriendRequest(user.getUsername(), id);
  }

  @Override
  public void removeFriend(UserDto user, Long id) {
    serviceRecordClient.removeFriend(user.getUsername(), id);
  }

  @Override
  public void updateFriendNickname(UserDto user, Long id, String nickname) {
    serviceRecordClient.updateFriendNickname(user.getUsername(), id, nickname);
  }

  @Override
  public ChatMessageDto sendMessage(ChatMessageDto dto) {
    ChatMessageDto saved = serviceRecordClient.sendMessage(dto).getData();
    if (saved != null && saved.getRecipientUserId() != null) {
      onlineSseService.sendToUser(
          saved.getRecipientUserId(),
          SseEmitter.event().name("new-message").data(saved));
    }
    return saved;
  }

  @Override
  public List<ChatMessageDto> getMessages(String roomId, Long since) {
    return serviceRecordClient.getMessages(roomId, since).getData();
  }

  @Override
  public void markRoomRead(String recipientUserId, String roomId) {
    serviceRecordClient.markRoomRead(recipientUserId, roomId);
  }

  @Override
  public Map<String, Long> getUnreadCounts(String recipientUserId) {
    return serviceRecordClient.getUnreadCounts(recipientUserId).getData();
  }
}
