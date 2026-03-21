package com.heloword.frontendapi.service.social;

import java.util.List;
import java.util.Map;
import com.heloword.common.model.dto.ChatMessageDto;
import com.heloword.common.model.dto.FriendDto;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.model.request.HeartbeatRequest;
import com.heloword.frontendapi.model.response.FriendResponseDto;
import com.heloword.frontendapi.model.response.OnlineUserDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SocialFrontendService {

  void heartbeat(HeartbeatRequest request);

  void removeHeartbeat(String userId);

  List<OnlineUserDto> getOnlineUsers();

  /** Create a per-user SSE emitter and immediately send the current online list. */
  SseEmitter subscribeForUser(String userId);

  List<FriendResponseDto> getFriends(UserDto user);

  FriendDto sendFriendRequest(UserDto user, String addresseeUsername);

  void acceptFriendRequest(UserDto user, Long id);

  void rejectFriendRequest(UserDto user, Long id);

  void removeFriend(UserDto user, Long id);

  void updateFriendNickname(UserDto user, Long id, String nickname);

  ChatMessageDto sendMessage(ChatMessageDto dto);

  List<ChatMessageDto> getMessages(String roomId, Long since);

  void markRoomRead(String recipientUserId, String roomId);

  Map<String, Long> getUnreadCounts(String recipientUserId);
}
