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
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.type.ResponseCode;
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
      log.debug("getFriends — caller={} requester={} addressee={} iAmRequester={}",
          user.getUsername(), requesterUsername, addresseeUsername, iAmRequester);
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
      log.info("sendFriendRequest — requester={} addressee=[{}] len={}",
          user.getUsername(), addresseeUsername, addresseeUsername == null ? -1 : addresseeUsername.length());
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
    // Addressee's own display name — stored as requesterNickname so the requester
    // immediately sees a real name for the friend who just accepted them.
    String addresseeDisplayName = user.getNickname() != null ? user.getNickname()
        : user.getFullname() != null ? user.getFullname()
        : user.getUsername();

    var result = serviceRecordClient.acceptFriendRequest(user.getUsername(), id, addresseeDisplayName);
    var accepted = result.getData();
    if (accepted == null) {
      // Service-record rejected the request (e.g. "Not authorized", "not found").
      // Propagate as an error so the frontend receives a failure response instead
      // of a false success that leaves the pending request stuck in the UI.
      log.error("acceptFriendRequest failed — user={} id={} code={} msg={}",
          user.getUsername(), id, result.getCode(), result.getMessage());
      throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
    }

    // Also pre-fill addresseeNickname (what the accepting user sees for the requester)
    // with the requester's live display name if they are currently online.
    // Only do this if the column is still empty to avoid overwriting a custom nickname.
    if (accepted.getAddresseeNickname() == null && accepted.getRequesterUsername() != null) {
      String requesterUsername = decodeUsername(accepted.getRequesterUsername());
      getOnlineUsers().stream()
          .filter(u -> requesterUsername.equals(u.getUserId()))
          .map(OnlineUserDto::getDisplayName)
          .findFirst()
          .ifPresent(displayName -> {
            try {
              serviceRecordClient.updateFriendNickname(user.getUsername(), id, displayName);
            } catch (Exception e) {
              log.warn("acceptFriendRequest — could not set default addresseeNickname for id={}: {}", id, e.getMessage());
            }
          });
    }

    // Notify the original requester so their friends list refreshes automatically
    // and they see the ACCEPTED state without needing to reload the page.
    if (accepted.getRequesterUsername() != null) {
      String requester = decodeUsername(accepted.getRequesterUsername());
      socialPushService.sendFriendRequestToUser(requester, user.getUsername());
    }
  }

  @Override
  public void rejectFriendRequest(UserDto user, Long id) {
    var result = serviceRecordClient.rejectFriendRequest(user.getUsername(), id);
    if (result == null || !ResponseCode.SUCCESS.getCode().equals(result.getCode())) {
      log.error("rejectFriendRequest failed — user={} id={} code={} msg={}",
          user.getUsername(), id, result != null ? result.getCode() : "null", result != null ? result.getMessage() : "null");
      throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
    }
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

  /** Decode percent-encoded usernames from legacy records (e.g. '%40' → '@').
   *  Also strips trailing '=' left by Feign's form-body encoding bug. */
  private static String decodeUsername(String value) {
    if (value == null) return null;
    try {
      String decoded = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
      return decoded.endsWith("=") ? decoded.substring(0, decoded.length() - 1) : decoded;
    } catch (Exception e) {
      return value;
    }
  }
}
