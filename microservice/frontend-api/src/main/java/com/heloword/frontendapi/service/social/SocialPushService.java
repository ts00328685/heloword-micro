package com.heloword.frontendapi.service.social;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.heloword.common.model.dto.ChatMessageDto;
import com.heloword.frontendapi.model.response.OnlineUserDto;

@Log4j2
@Service
@AllArgsConstructor
public class SocialPushService {

  private final SimpMessagingTemplate messagingTemplate;

  /** Broadcast the current online-user list to every connected WebSocket client. */
  public void broadcastOnlineUsers(List<OnlineUserDto> onlineUsers) {
    messagingTemplate.convertAndSend("/topic/online-users", onlineUsers);
  }

  /** Push a new chat message to the recipient's personal topic. */
  public void sendMessageToUser(String recipientUserId, ChatMessageDto message) {
    messagingTemplate.convertAndSend("/topic/social/" + recipientUserId + "/messages", message);
    log.debug("Pushed new-message to /topic/social/{}/messages", recipientUserId);
  }

  /** Push a friend-request notification to the addressee's personal topic. */
  public void sendFriendRequestToUser(String addresseeId, String requesterUsername) {
    messagingTemplate.convertAndSend("/topic/social/" + addresseeId + "/friend-requests", requesterUsername);
    log.debug("Pushed new-friend-request to /topic/social/{}/friend-requests", addresseeId);
  }
}
