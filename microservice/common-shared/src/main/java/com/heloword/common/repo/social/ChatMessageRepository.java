package com.heloword.common.repo.social;

import java.util.Date;
import java.util.List;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.social.ChatMessageEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends IBaseRepo<ChatMessageEntity, Long> {

  List<ChatMessageEntity> findAllByRoomIdOrderBySentAtAsc(String roomId);

  List<ChatMessageEntity> findAllByRoomIdAndSentAtAfterOrderBySentAtAsc(String roomId, Date since);

  List<ChatMessageEntity> findAllByRecipientUserIdAndReadAtIsNull(String recipientUserId);

  @Query("SELECT m FROM ChatMessageEntity m WHERE m.senderUserId = :userId OR m.recipientUserId = :userId ORDER BY m.sentAt DESC")
  List<ChatMessageEntity> findAllByUserId(@Param("userId") String userId);
}
