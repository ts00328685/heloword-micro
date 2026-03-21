package com.heloword.common.repo.social;

import java.util.List;
import java.util.Optional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.social.FriendEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRepository extends IBaseRepo<FriendEntity, Long> {

  List<FriendEntity> findAllByRequesterUsernameAndFriendStatus(String requesterUsername, String friendStatus);

  List<FriendEntity> findAllByAddresseeUsernameAndFriendStatus(String addresseeUsername, String friendStatus);

  List<FriendEntity> findAllByRequesterUsername(String requesterUsername);

  List<FriendEntity> findAllByAddresseeUsername(String addresseeUsername);

  Optional<FriendEntity> findByRequesterUsernameAndAddresseeUsername(
      String requesterUsername, String addresseeUsername);
}
