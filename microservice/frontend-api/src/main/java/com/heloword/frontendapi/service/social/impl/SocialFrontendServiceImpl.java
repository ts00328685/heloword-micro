package com.heloword.frontendapi.service.social.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import com.heloword.frontendapi.service.social.SocialFrontendService;
import com.heloword.frontendapi.service.social.SocialPushService;

@Log4j2
@Service
@AllArgsConstructor
public class SocialFrontendServiceImpl implements SocialFrontendService {

  private static final String ONLINE_KEY = "social:online";
  /** Users are considered online if they heartbeated within 90 seconds */
  private static final long ONLINE_WINDOW_MS = 90_000L;

  private RedisTemplate<String, Object> redisTemplate;
  private ServiceRecordClient serviceRecordClient;
  private SocialPushService socialPushService;

  @Override
  public void heartbeat(HeartbeatRequest request) {
    double score = System.currentTimeMillis();
    String member = request.getUserId() + "|" + request.getDisplayName() + "|" + (Boolean.TRUE.equals(request.getIsGuest()) ? "1" : "0");
    redisTemplate.opsForZSet().add(ONLINE_KEY, member, score);
    // Push updated list to all WebSocket subscribers immediately
    socialPushService.broadcastOnlineUsers(getOnlineUsers());
  }

  @Override
  public void removeHeartbeat(String userId) {
    long cutoff = System.currentTimeMillis() - ONLINE_WINDOW_MS;
    Set<Object> members = redisTemplate.opsForZSet().rangeByScore(ONLINE_KEY, cutoff, Double.MAX_VALUE);
    if (members != null) {
      members.stream()
          .filter(m -> ((String) m).startsWith(userId + "|"))
          .forEach(m -> redisTemplate.opsForZSet().remove(ONLINE_KEY, m));
    }
    socialPushService.broadcastOnlineUsers(getOnlineUsers());
  }

  @Override
  public List<OnlineUserDto> getOnlineUsers() {
    long cutoff = System.currentTimeMillis() - ONLINE_WINDOW_MS;
    redisTemplate.opsForZSet().removeRangeByScore(ONLINE_KEY, 0, cutoff - 1);
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

    Set<String> onlineIds = getOnlineUsers().stream()
        .map(OnlineUserDto::getUserId)
        .collect(Collectors.toSet());

    return friends.stream().map(f -> {
      // Usernames may have been stored URL-encoded (Feign 10.x encodes '@' → '%40')
      // in older records; decode them so display and comparisons use the raw value.
      String requesterUsername = decodeUsername(f.getRequesterUsername());
      String addresseeUsername = decodeUsername(f.getAddresseeUsername());

      boolean iAmRequester = user.getUsername().equals(requesterUsername);
      String otherUserId = iAmRequester ? addresseeUsername : requesterUsername;
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
    try {
      FriendDto saved = serviceRecordClient.sendFriendRequest(user.getUsername(), addresseeUsername).getData();
      socialPushService.sendFriendRequestToUser(addresseeUsername, user.getUsername());
      return saved;
    } catch (Exception e) {
      log.error("sendFriendRequest feign call failed — requester={} addressee={}: {}",
          user.getUsername(), addresseeUsername, e.getMessage(), e);
      throw e;
    }
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
    try {
      ChatMessageDto saved = serviceRecordClient.sendMessage(dto).getData();
      if (saved != null && saved.getRecipientUserId() != null) {
        socialPushService.sendMessageToUser(saved.getRecipientUserId(), saved);
      }
      return saved;
    } catch (Exception e) {
      log.error("sendMessage feign call failed — sender={} recipient={}: {}",
          dto.getSenderUserId(), dto.getRecipientUserId(), e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<ChatMessageDto> getMessages(String roomId, Long since) {
    try {
      return serviceRecordClient.getMessages(roomId, since).getData();
    } catch (Exception e) {
      log.error("getMessages feign call failed — roomId={}: {}", roomId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public void markRoomRead(String recipientUserId, String roomId) {
    try {
      serviceRecordClient.markRoomRead(recipientUserId, roomId);
    } catch (Exception e) {
      log.warn("markRoomRead feign call failed — recipientUserId={} roomId={}: {}",
          recipientUserId, roomId, e.getMessage());
    }
  }

  @Override
  public Map<String, Long> getUnreadCounts(String recipientUserId) {
    try {
      return serviceRecordClient.getUnreadCounts(recipientUserId).getData();
    } catch (Exception e) {
      log.error("getUnreadCounts feign call failed — recipientUserId={}: {}",
          recipientUserId, e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public List<ChatMessageDto> getChatRooms(String userId) {
    try {
      return serviceRecordClient.getChatRooms(userId).getData();
    } catch (Exception e) {
      log.error("getChatRooms feign call failed — userId={}: {}", userId, e.getMessage(), e);
      throw e;
    }
  }

  /** Decode percent-encoded usernames from legacy records (e.g. '%40' → '@'). */
  private static String decodeUsername(String value) {
    if (value == null) return null;
    try {
      return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      return value;
    }
  }
}
