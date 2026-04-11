package com.heloword.record.service;

import java.util.List;
import java.util.Optional;
import com.heloword.common.model.dto.SharedVocabGroupDto;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;

public interface SharedVocabService {

  SharedVocabGroupDto requestShare(String requesterUsername, String requesterUuid,
      String requesterDisplayName, Long groupId);

  Optional<SharedVocabGroupDto> getShareStatus(String requesterUsername, Long groupId);

  List<SharedVocabGroupDto> getApprovedGroups();

  List<UserCustomWordDto> getGroupWords(Long shareId);

  List<SharedVocabGroupDto> getPendingRequests();

  void approve(Long shareId);

  void reject(Long shareId);

  UserCustomGroupDto copyToUser(String targetUsername, Long shareId);

  void deleteSharedGroup(Long shareId);
}
