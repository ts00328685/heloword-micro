package com.heloword.record.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.heloword.common.entity.social.ChatMessageEntity;
import com.heloword.common.entity.social.FriendEntity;

public interface SocialService {

  // ── Friends ──────────────────────────────────────────────────────────────

  List<FriendEntity> getFriends(String username);

  FriendEntity sendFriendRequest(String requesterUsername, String addresseeUsername);

  FriendEntity acceptFriendRequest(String addresseeUsername, Long id, String addresseeDisplayName);

  void rejectFriendRequest(String addresseeUsername, Long id);

  void removeFriend(String username, Long id);

  void updateFriendNickname(String username, Long id, String nickname);

  // ── Chat ──────────────────────────────────────────────────────────────────

  ChatMessageEntity sendMessage(ChatMessageEntity message);

  List<ChatMessageEntity> getMessages(String roomId, Long sinceMs);

  void markRoomRead(String recipientUserId, String roomId);

  Map<String, Long> getUnreadCounts(String recipientUserId);

  List<ChatMessageEntity> getChatRooms(String userId);
}
