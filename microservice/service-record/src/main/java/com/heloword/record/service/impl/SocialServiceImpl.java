package com.heloword.record.service.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    return Stream.concat(
        friendRepository.findAllByRequesterUsername(username).stream(),
        friendRepository.findAllByAddresseeUsername(username).stream()
    ).collect(Collectors.toList());
  }

  @Override
  public FriendEntity sendFriendRequest(String requesterUsername, String addresseeUsername) {
    // Check if friendship already exists in either direction
    if (friendRepository.findByRequesterUsernameAndAddresseeUsername(requesterUsername, addresseeUsername).isPresent()) {
      throw new IllegalStateException("Friend request already exists");
    }
    if (friendRepository.findByRequesterUsernameAndAddresseeUsername(addresseeUsername, requesterUsername).isPresent()) {
      throw new IllegalStateException("Friend request already exists");
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
    if (!entity.getAddresseeUsername().equals(addresseeUsername)) {
      throw new IllegalStateException("Not authorized");
    }
    entity.setFriendStatus("ACCEPTED");
    return friendRepository.save(entity);
  }

  @Override
  public void rejectFriendRequest(String addresseeUsername, Long id) {
    FriendEntity entity = friendRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
    if (!entity.getAddresseeUsername().equals(addresseeUsername)) {
      throw new IllegalStateException("Not authorized");
    }
    friendRepository.delete(entity);
  }

  @Override
  public void removeFriend(String username, Long id) {
    FriendEntity entity = friendRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Friend not found"));
    if (!entity.getRequesterUsername().equals(username) && !entity.getAddresseeUsername().equals(username)) {
      throw new IllegalStateException("Not authorized");
    }
    friendRepository.delete(entity);
  }

  @Override
  public void updateFriendNickname(String username, Long id, String nickname) {
    FriendEntity entity = friendRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Friend not found"));
    if (entity.getRequesterUsername().equals(username)) {
      entity.setRequesterNickname(nickname);
    } else if (entity.getAddresseeUsername().equals(username)) {
      entity.setAddresseeNickname(nickname);
    } else {
      throw new IllegalStateException("Not authorized");
    }
    friendRepository.save(entity);
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
}
