package com.heloword.record.service;

import java.util.List;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.announcement.OfficialMessageEntity;

public interface OfficialMessageService extends IBaseService<OfficialMessageEntity, Long> {

  List<OfficialMessageEntity> findAllOrderByPublishedAtDesc();
}
