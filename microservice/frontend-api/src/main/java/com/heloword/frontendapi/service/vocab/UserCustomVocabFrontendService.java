package com.heloword.frontendapi.service.vocab;

import java.util.List;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.common.model.dto.UserDto;

public interface UserCustomVocabFrontendService {

  List<UserCustomGroupDto> getGroups(UserDto user);

  UserCustomGroupDto createGroup(UserDto user, UserCustomGroupDto dto);

  UserCustomGroupDto updateGroup(UserDto user, Long id, UserCustomGroupDto dto);

  void deleteGroup(UserDto user, Long id);

  List<UserCustomWordDto> getWords(UserDto user, Long groupId);

  UserCustomWordDto addWord(UserDto user, Long groupId, UserCustomWordDto dto);

  List<UserCustomWordDto> batchAddWords(UserDto user, Long groupId, List<UserCustomWordDto> dtos);

  UserCustomWordDto updateWord(UserDto user, Long wordId, UserCustomWordDto dto);

  void deleteWord(UserDto user, Long wordId);
}
