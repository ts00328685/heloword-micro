package com.heloword.record.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.announcement.OfficialMessageEntity;
import com.heloword.common.repo.announcement.OfficialMessageRepository;
import com.heloword.record.service.OfficialMessageService;

@Service
public class OfficialMessageServiceImpl extends AbstractBaseServiceImpl<OfficialMessageEntity, Long>
    implements OfficialMessageService {

  @Autowired
  private OfficialMessageRepository officialMessageRepository;

  @Override
  protected IBaseRepo<OfficialMessageEntity, Long> getRepo() {
    return officialMessageRepository;
  }

  @Override
  public List<OfficialMessageEntity> findAllOrderByPublishedAtDesc() {
    return officialMessageRepository.findAllByOrderByPublishedAtDesc();
  }
}
