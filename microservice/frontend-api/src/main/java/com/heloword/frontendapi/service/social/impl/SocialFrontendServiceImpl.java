package com.heloword.frontendapi.service.social.impl;

import java.net.URLDecoder;
import java.net.URLEncoder;
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
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.feignclient.ServiceUserClient;
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
  private ServiceUserClient serviceUserClient;
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
      String otherUsername = iAmRequester ? addresseeUsername : requesterUsername;
      String myNickname = iAmRequester ? f.getRequesterNickname() : f.getAddresseeNickname();

      // Resolve email-based username to UUID + profile display name in one call
      MemberEntity otherMember = null;
      try {
        otherMember = serviceUserClient.getMemberByEmail(otherUsername).getData();
      } catch (Exception e) {
        log.warn("getFriends — lookup failed for otherUsername={}: {}", otherUsername, e.getMessage());
      }
      String otherUserId = (otherMember != null && otherMember.getUuid() != null)
          ? otherMember.getUuid() : otherUsername;
      String otherProfileName = otherMember != null && otherMember.getNickname() != null
          ? otherMember.getNickname()
          : otherMember != null && otherMember.getFullname() != null
              ? otherMember.getFullname()
              : null;

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
          .displayName(myNickname != null ? myNickname : otherProfileName != null ? otherProfileName : otherUserId)
          .myNickname(myNickname)
          .status(status)
          .isOnline(onlineIds.contains(otherUserId))
          .build();
    }).collect(Collectors.toList());
  }

  /** Resolves a username (email) to the member's UUID. Falls back to the original value on failure. */
  private String resolveUsernameToUuid(String username) {
    if (username == null) return null;
    try {
      MemberEntity member = serviceUserClient.getMemberByEmail(username).getData();
      if (member != null && member.getUuid() != null) {
        return member.getUuid();
      }
    } catch (Exception e) {
      log.warn("resolveUsernameToUuid — lookup failed for username={}: {}", username, e.getMessage());
    }
    return username; // fallback: return email if UUID not yet populated
  }

  /** UUID pattern: 8-4-4-4-12 hex chars, with optional trailing '=' from Feign encoding */
  private static final java.util.regex.Pattern UUID_PATTERN =
      java.util.regex.Pattern.compile(
          "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}=*",
          java.util.regex.Pattern.CASE_INSENSITIVE);

  /**
   * If addresseeUsername looks like a UUID (online-list userId after the heartbeat UUID change),
   * resolve it to the actual username via the user service before forwarding to service-record.
   */
  private String resolveAddresseeUsername(String input) {
    if (input == null) return null;
    String stripped = input.replaceAll("=+$", ""); // strip Feign-appended '='
    if (UUID_PATTERN.matcher(stripped).matches()) {
      try {
        MemberEntity member = serviceUserClient.getMemberByUuid(stripped).getData();
        if (member != null && member.getUsername() != null) {
          log.debug("resolveAddresseeUsername — resolved uuid={} to username={}", stripped, member.getUsername());
          return member.getUsername();
        }
        log.warn("resolveAddresseeUsername — uuid={} not found in user service", stripped);
        return null;
      } catch (Exception e) {
        log.error("resolveAddresseeUsername — lookup failed for uuid={}: {}", stripped, e.getMessage());
        return null;
      }
    }
    return input;
  }

  @Override
  public FriendDto sendFriendRequest(UserDto user, String addresseeUsername) {
    try {
      String resolvedAddressee = resolveAddresseeUsername(addresseeUsername);
      log.info("sendFriendRequest — requester={} addressee=[{}] resolved=[{}]",
          user.getUsername(), addresseeUsername, resolvedAddressee);
      if (resolvedAddressee == null) {
        throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
      }
      FriendDto saved = serviceRecordClient.sendFriendRequest(user.getUsername(), resolvedAddressee).getData();
      socialPushService.sendFriendRequestToUser(resolvedAddressee, user.getUsername());
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

    var result = serviceRecordClient.acceptFriendRequest(user.getUsername(), id, encodeHeader(addresseeDisplayName));
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
              serviceRecordClient.updateFriendNickname(user.getUsername(), id, encodeHeader(displayName));
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
    serviceRecordClient.updateFriendNickname(user.getUsername(), id, encodeHeader(nickname));
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

  /** URL-encode a string for safe transmission as an HTTP header value.
   *  Necessary for non-ASCII content (e.g. Chinese display names) because
   *  HTTP/1.1 headers are Latin-1; the receiver decodes with URLDecoder. */
  private static String encodeHeader(String value) {
    if (value == null) return null;
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      return value;
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
