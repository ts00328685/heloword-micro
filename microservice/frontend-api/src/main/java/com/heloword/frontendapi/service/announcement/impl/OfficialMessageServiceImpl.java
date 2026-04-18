package com.heloword.frontendapi.service.announcement.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.frontendapi.model.response.OfficialMessageDto;
import com.heloword.frontendapi.service.announcement.OfficialMessageService;

@Log4j2
@Service
@AllArgsConstructor
public class OfficialMessageServiceImpl implements OfficialMessageService {

  private ServiceRecordClient serviceRecordClient;

  @Override
  public List<OfficialMessageDto> getAll() {
    var entities = serviceRecordClient.getOfficialMessages().getData();
    if (entities == null) return Collections.emptyList();
    return entities.stream()
        .map(e -> new OfficialMessageDto(e.getId(), e.getTitle(), e.getContent(), e.getPublishedAt()))
        .collect(Collectors.toList());
  }
}
