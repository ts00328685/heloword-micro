package com.heloword.record.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.announcement.OfficialMessageEntity;
import com.heloword.record.service.OfficialMessageService;

@RestController
@RequestMapping("/official-message")
public class OfficialMessageRestController extends AbstractBaseRestController<OfficialMessageEntity, Long> {

  @Autowired
  private OfficialMessageService officialMessageService;

  @Override
  public IBaseService<OfficialMessageEntity, Long> getService() {
    return officialMessageService;
  }

  @GetMapping("/all")
  public HeloResponse<?> getAll() {
    return success(officialMessageService.findAllOrderByPublishedAtDesc());
  }
}
