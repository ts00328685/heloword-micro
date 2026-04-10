package com.heloword.common.model.dto;

import java.util.Date;
import com.heloword.common.entity.social.ChatMessageEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class ChatMessageDto {
  private Long id;
  private String senderUserId;
  private String senderDisplayName;
  private String recipientUserId;
  private String recipientDisplayName;
  private String roomId;
  private String content;
  private Date readAt;
  private Date sentAt;

  public static ChatMessageEntity toEntity(ChatMessageDto dto) {
    ChatMessageEntity entity = new ChatMessageEntity();
    BeanUtils.copyProperties(dto, entity);
    return entity;
  }

  public static ChatMessageDto fromEntity(ChatMessageEntity entity) {
    ChatMessageDto dto = new ChatMessageDto();
    dto.setId(entity.getId());
    dto.setSenderUserId(entity.getSenderUserId());
    dto.setSenderDisplayName(entity.getSenderDisplayName());
    dto.setRecipientUserId(entity.getRecipientUserId());
    dto.setRecipientDisplayName(entity.getRecipientDisplayName());
    dto.setRoomId(entity.getRoomId());
    dto.setContent(entity.getContent());
    dto.setReadAt(entity.getReadAt());
    dto.setSentAt(entity.getSentAt());
    return dto;
  }
}
