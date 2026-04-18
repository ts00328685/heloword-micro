package com.heloword.common.repo.announcement;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.announcement.OfficialMessageEntity;

@Repository
public interface OfficialMessageRepository extends IBaseRepo<OfficialMessageEntity, Long> {

  List<OfficialMessageEntity> findAllByOrderByPublishedAtDesc();
}
