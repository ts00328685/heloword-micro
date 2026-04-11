package com.heloword.frontendapi.service.vocab;

import java.util.List;
import java.util.Optional;
import com.heloword.common.model.dto.SharedVocabGroupDto;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.common.model.dto.UserDto;

public interface SharedVocabFrontendService {

  List<SharedVocabGroupDto> getApprovedGroups();

  List<UserCustomWordDto> getGroupWords(Long shareId);

  SharedVocabGroupDto requestShare(UserDto user, Long groupId);

  Optional<SharedVocabGroupDto> getShareStatus(UserDto user, Long groupId);

  List<SharedVocabGroupDto> getPendingRequests();

  void approve(Long shareId);

  void reject(Long shareId);

  UserCustomGroupDto copyToUser(UserDto user, Long shareId);

  void deleteSharedGroup(Long shareId);
}
