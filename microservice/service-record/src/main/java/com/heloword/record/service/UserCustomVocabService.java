package com.heloword.record.service;

import java.util.List;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;

public interface UserCustomVocabService {

  List<UserCustomGroupDto> getGroups(String username);

  UserCustomGroupDto createGroup(String username, UserCustomGroupDto dto);

  UserCustomGroupDto updateGroup(String username, Long id, UserCustomGroupDto dto);

  void deleteGroup(String username, Long id);

  List<UserCustomWordDto> getWords(String username, Long groupId);

  UserCustomWordDto addWord(String username, Long groupId, UserCustomWordDto dto);

  List<UserCustomWordDto> batchAddWords(String username, Long groupId, List<UserCustomWordDto> dtos);

  UserCustomWordDto updateWord(String username, Long wordId, UserCustomWordDto dto);

  void deleteWord(String username, Long wordId);
}
