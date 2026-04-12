package com.heloword.frontendapi.service.vocab.impl;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.service.vocab.UserCustomVocabFrontendService;

@Log4j2
@Service
@AllArgsConstructor
public class UserCustomVocabFrontendServiceImpl implements UserCustomVocabFrontendService {

  private ServiceRecordClient serviceRecordClient;

  @Override
  public List<UserCustomGroupDto> getGroups(UserDto user) {
    return serviceRecordClient.getCustomGroups(user.getUsername()).getData();
  }

  @Override
  public UserCustomGroupDto createGroup(UserDto user, UserCustomGroupDto dto) {
    return serviceRecordClient.createCustomGroup(user.getUsername(), dto).getData();
  }

  @Override
  public UserCustomGroupDto updateGroup(UserDto user, Long id, UserCustomGroupDto dto) {
    return serviceRecordClient.updateCustomGroup(user.getUsername(), id, dto).getData();
  }

  @Override
  public void deleteGroup(UserDto user, Long id) {
    serviceRecordClient.deleteCustomGroup(user.getUsername(), id);
  }

  @Override
  public List<UserCustomWordDto> getWords(UserDto user, Long groupId) {
    return serviceRecordClient.getCustomWords(user.getUsername(), groupId).getData();
  }

  @Override
  public UserCustomWordDto addWord(UserDto user, Long groupId, UserCustomWordDto dto) {
    return serviceRecordClient.addCustomWord(user.getUsername(), groupId, dto).getData();
  }

  @Override
  public List<UserCustomWordDto> batchAddWords(UserDto user, Long groupId, List<UserCustomWordDto> dtos) {
    return serviceRecordClient.batchAddCustomWords(user.getUsername(), groupId, dtos).getData();
  }

  @Override
  public UserCustomWordDto updateWord(UserDto user, Long wordId, UserCustomWordDto dto) {
    return serviceRecordClient.updateCustomWord(user.getUsername(), wordId, dto).getData();
  }

  @Override
  public void deleteWord(UserDto user, Long wordId) {
    serviceRecordClient.deleteCustomWord(user.getUsername(), wordId);
  }
}
