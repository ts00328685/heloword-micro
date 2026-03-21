package com.heloword.common.entity.social;

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
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "friend_seq")
@Table(name = "FRIEND")
public class FriendEntity extends BaseEntity {
  /** Username of the user who sent the friend request */
  private String requesterUsername;
  /** Username of the user who received the friend request */
  private String addresseeUsername;
  /** PENDING / ACCEPTED */
  private String friendStatus;
  /** Nickname the requester uses for the addressee */
  private String requesterNickname;
  /** Nickname the addressee uses for the requester */
  private String addresseeNickname;
}
