package com.heloword.common.entity.social;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "chat_message_seq")
@Table(name = "CHAT_MESSAGE")
public class ChatMessageEntity extends BaseEntity {
  /** Sender identifier: username (logged-in) or guest UUID */
  private String senderUserId;
  /** Sender display name at time of sending */
  private String senderDisplayName;
  /** Recipient identifier: username (logged-in) or guest UUID */
  private String recipientUserId;
  private String recipientDisplayName;
  /** Computed room id: sorted(sender, recipient).join(':') */
  private String roomId;
  @Column(length = 4096)
  private String content;
  /** Timestamp when recipient read the message; null = unread */
  private Date readAt;
  /** When the message was sent */
  private Date sentAt;
}
