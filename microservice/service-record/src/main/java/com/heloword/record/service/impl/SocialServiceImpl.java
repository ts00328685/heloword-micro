package com.heloword.record.service.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.entity.social.ChatMessageEntity;
import com.heloword.common.entity.social.FriendEntity;
import com.heloword.common.repo.social.ChatMessageRepository;
import com.heloword.common.repo.social.FriendRepository;
import com.heloword.record.service.SocialService;

@Service
public class SocialServiceImpl implements SocialService {

  @Autowired
  private FriendRepository friendRepository;

  @Autowired
  private ChatMessageRepository chatMessageRepository;

  // ── Friends ──────────────────────────────────────────────────────────────

  @Override
  public List<FriendEntity> getFriends(String username) {
    // Also search with URL-encoded form for legacy corrupted records (e.g. '%40' for '@')
    String encodedUsername = encodeUsername(username);
    return Stream.of(
        friendRepository.findAllByRequesterUsername(username),
        friendRepository.findAllByAddresseeUsername(username),
        encodedUsername.equals(username) ? List.<FriendEntity>of() : friendRepository.findAllByRequesterUsername(encodedUsername),
        encodedUsername.equals(username) ? List.<FriendEntity>of() : friendRepository.findAllByAddresseeUsername(encodedUsername)
    ).flatMap(List::stream)
     .distinct()
     .collect(Collectors.toList());
  }

  @Override
  public FriendEntity sendFriendRequest(String requesterUsername, String addresseeUsername) {
    String encodedRequester = encodeUsername(requesterUsername);
    String encodedAddressee = encodeUsername(addresseeUsername);

    // If a request was already sent in this direction, return it idempotently
    // (handles duplicate clicks or retries gracefully).
    Optional<FriendEntity> existing = friendRepository.findByRequesterUsernameAndAddresseeUsername(requesterUsername, addresseeUsername);
    if (!existing.isPresent()) {
      existing = friendRepository.findByRequesterUsernameAndAddresseeUsername(encodedRequester, addresseeUsername);
    }
    if (!existing.isPresent()) {
      existing = friendRepository.findByRequesterUsernameAndAddresseeUsername(requesterUsername, encodedAddressee);
    }
    if (existing.isPresent()) return existing.get();

    // Check the reverse direction — if they already sent a request to me, reject.
    if (friendRepository.findByRequesterUsernameAndAddresseeUsername(addresseeUsername, requesterUsername).isPresent()
        || friendRepository.findByRequesterUsernameAndAddresseeUsername(encodedAddressee, requesterUsername).isPresent()
        || friendRepository.findByRequesterUsernameAndAddresseeUsername(addresseeUsername, encodedRequester).isPresent()) {
      throw new IllegalStateException("A friend request from that user is already pending");
    }

    FriendEntity entity = FriendEntity.builder()
        .requesterUsername(requesterUsername)
        .addresseeUsername(addresseeUsername)
        .friendStatus("PENDING")
        .build();
    return friendRepository.save(entity);
  }

  @Override
  public FriendEntity acceptFriendRequest(String addresseeUsername, Long id) {
    FriendEntity entity = friendRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
    // Decode stored value to handle legacy corrupted records
    if (!decodeUsername(entity.getAddresseeUsername()).equals(addresseeUsername)) {
      throw new IllegalStateException("Not authorized");
    }
    entity.setFriendStatus("ACCEPTED");
    // Self-heal: fix the stored username if it was corrupted
    entity.setAddresseeUsername(addresseeUsername);
    entity.setRequesterUsername(decodeUsername(entity.getRequesterUsername()));
    return friendRepository.save(entity);
  }

  @Override
  public void rejectFriendRequest(String addresseeUsername, Long id) {
    FriendEntity entity = friendRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
    if (!decodeUsername(entity.getAddresseeUsername()).equals(addresseeUsername)) {
      throw new IllegalStateException("Not authorized");
    }
    friendRepository.delete(entity);
  }

  @Override
  public void removeFriend(String username, Long id) {
    FriendEntity entity = friendRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Friend not found"));
    if (!decodeUsername(entity.getRequesterUsername()).equals(username)
        && !decodeUsername(entity.getAddresseeUsername()).equals(username)) {
      throw new IllegalStateException("Not authorized");
    }
    friendRepository.delete(entity);
  }

  @Override
  public void updateFriendNickname(String username, Long id, String nickname) {
    FriendEntity entity = friendRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Friend not found"));
    if (decodeUsername(entity.getRequesterUsername()).equals(username)) {
      entity.setRequesterNickname(nickname);
    } else if (decodeUsername(entity.getAddresseeUsername()).equals(username)) {
      entity.setAddresseeNickname(nickname);
    } else {
      throw new IllegalStateException("Not authorized");
    }
    friendRepository.save(entity);
  }

  private static String decodeUsername(String value) {
    if (value == null) return null;
    try {
      return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      return value;
    }
  }

  /** Returns the percent-encoded form of a username (e.g. '@' → '%40'). */
  private static String encodeUsername(String value) {
    if (value == null) return null;
    try {
      return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.name())
          .replace("+", "%20"); // encode spaces as %20, not +
    } catch (Exception e) {
      return value;
    }
  }

  // ── Chat ──────────────────────────────────────────────────────────────────

  @Override
  public ChatMessageEntity sendMessage(ChatMessageEntity message) {
    if (message.getSentAt() == null) {
      message.setSentAt(Date.from(Instant.now()));
    }
    // Compute roomId as sorted pair
    String[] users = {message.getSenderUserId(), message.getRecipientUserId()};
    java.util.Arrays.sort(users);
    message.setRoomId(users[0] + ":" + users[1]);
    return chatMessageRepository.save(message);
  }

  @Override
  public List<ChatMessageEntity> getMessages(String roomId, Long sinceMs) {
    if (sinceMs != null && sinceMs > 0) {
      return chatMessageRepository.findAllByRoomIdAndSentAtAfterOrderBySentAtAsc(roomId, new Date(sinceMs));
    }
    return chatMessageRepository.findAllByRoomIdOrderBySentAtAsc(roomId);
  }

  @Override
  public void markRoomRead(String recipientUserId, String roomId) {
    List<ChatMessageEntity> unread = chatMessageRepository.findAllByRoomIdOrderBySentAtAsc(roomId).stream()
        .filter(m -> m.getRecipientUserId().equals(recipientUserId) && m.getReadAt() == null)
        .collect(Collectors.toList());
    Date now = Date.from(Instant.now());
    unread.forEach(m -> m.setReadAt(now));
    chatMessageRepository.saveAll(unread);
  }

  @Override
  public Map<String, Long> getUnreadCounts(String recipientUserId) {
    return chatMessageRepository.findAllByRecipientUserIdAndReadAtIsNull(recipientUserId).stream()
        .collect(Collectors.groupingBy(ChatMessageEntity::getSenderUserId, Collectors.counting()));
  }

  @Override
  public List<ChatMessageEntity> getChatRooms(String userId) {
    // Returns the latest message per room for the given user, sorted by most recent first.
    // findAllByUserId returns all messages DESC by sentAt, so the first entry per roomId is the latest.
    return chatMessageRepository.findAllByUserId(userId).stream()
        .collect(Collectors.toMap(
            ChatMessageEntity::getRoomId,
            m -> m,
            (existing, newer) -> existing,  // keep first = latest (DESC order)
            LinkedHashMap::new
        ))
        .values().stream()
        .sorted(Comparator.comparing(ChatMessageEntity::getSentAt).reversed())
        .collect(Collectors.toList());
  }
}
