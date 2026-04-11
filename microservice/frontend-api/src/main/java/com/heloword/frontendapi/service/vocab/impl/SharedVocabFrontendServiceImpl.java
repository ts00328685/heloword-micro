package com.heloword.frontendapi.service.vocab.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.model.dto.SharedVocabGroupDto;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.service.vocab.SharedVocabFrontendService;

@Log4j2
@Service
@AllArgsConstructor
public class SharedVocabFrontendServiceImpl implements SharedVocabFrontendService {

  private ServiceRecordClient serviceRecordClient;

  @Override
  public List<SharedVocabGroupDto> getApprovedGroups() {
    return serviceRecordClient.getSharedGroups().getData();
  }

  @Override
  public List<UserCustomWordDto> getGroupWords(Long shareId) {
    return serviceRecordClient.getSharedGroupWords(shareId).getData();
  }

  @Override
  public SharedVocabGroupDto requestShare(UserDto user, Long groupId) {
    String displayName = resolveDisplayName(user);
    SharedVocabGroupDto dto = new SharedVocabGroupDto();
    dto.setGroupId(groupId);
    return serviceRecordClient.requestPublicShare(
        user.getUsername(), user.getUuid(), encodeHeader(displayName), dto).getData();
  }

  @Override
  public Optional<SharedVocabGroupDto> getShareStatus(UserDto user, Long groupId) {
    return Optional.ofNullable(
        serviceRecordClient.getPublicShareStatus(user.getUsername(), groupId).getData());
  }

  @Override
  public List<SharedVocabGroupDto> getPendingRequests() {
    return serviceRecordClient.getPendingSharedRequests().getData();
  }

  @Override
  public void approve(Long shareId) {
    serviceRecordClient.approveSharedRequest(shareId);
  }

  @Override
  public void reject(Long shareId) {
    serviceRecordClient.rejectSharedRequest(shareId);
  }

  @Override
  public void deleteSharedGroup(Long shareId) {
    serviceRecordClient.deleteSharedGroup(shareId);
  }

  @Override
  public UserCustomGroupDto copyToUser(UserDto user, Long shareId) {
    return serviceRecordClient.copySharedGroup(user.getUsername(), shareId).getData();
  }

  private String resolveDisplayName(UserDto user) {
    if (StringUtils.isNotBlank(user.getNickname())) return user.getNickname();
    if (StringUtils.isNotBlank(user.getFullname())) return user.getFullname();
    return user.getUuid();
  }

  private static String encodeHeader(String value) {
    if (value == null) return null;
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      return value;
    }
  }
}
