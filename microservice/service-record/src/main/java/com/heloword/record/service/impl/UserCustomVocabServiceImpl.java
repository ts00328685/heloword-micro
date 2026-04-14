package com.heloword.record.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.heloword.common.entity.vocab.UserCustomGroupEntity;
import com.heloword.common.entity.vocab.UserCustomWordEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.common.repo.vocab.UserCustomGroupRepository;
import com.heloword.common.repo.vocab.UserCustomWordRepository;
import com.heloword.common.type.ResponseCode;
import com.heloword.record.service.UserCustomVocabService;

@Slf4j
@Service
public class UserCustomVocabServiceImpl implements UserCustomVocabService {

  private static final int STATUS_ACTIVE = 1;
  private static final int MAX_GROUPS_PER_USER = 30;
  private static final int MAX_WORDS_PER_GROUP = 500;

  @Autowired
  private UserCustomGroupRepository groupRepo;

  @Autowired
  private UserCustomWordRepository wordRepo;

  // ── Groups ────────────────────────────────────────────────────────────────

  @Override
  public List<UserCustomGroupDto> getGroups(String username) {
    return groupRepo.findAllByUsernameAndStatus(username, STATUS_ACTIVE).stream()
        .map(g -> toGroupDto(g, (int) wordRepo.countByGroupIdAndStatus(g.getId(), STATUS_ACTIVE)))
        .collect(Collectors.toList());
  }

  @Override
  public UserCustomGroupDto createGroup(String username, UserCustomGroupDto dto) {
    if (groupRepo.countByUsernameAndStatus(username, STATUS_ACTIVE) >= MAX_GROUPS_PER_USER) {
      throw HeloServiceException.of(ResponseCode.GROUP_LIMIT_EXCEEDED);
    }
    UserCustomGroupEntity entity = UserCustomGroupEntity.builder()
        .username(username)
        .name(dto.getName())
        .description(dto.getDescription())
        .language(dto.getLanguage())
        .tags(dto.getTags())
        .status(STATUS_ACTIVE)
        .createDate(new Date())
        .updateDate(new Date())
        .build();
    entity = groupRepo.save(entity);
    return toGroupDto(entity, 0);
  }

  @Override
  public UserCustomGroupDto updateGroup(String username, Long id, UserCustomGroupDto dto) {
    UserCustomGroupEntity entity = groupRepo.findByIdAndUsername(id, username)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    entity.setName(dto.getName());
    entity.setDescription(dto.getDescription());
    entity.setLanguage(dto.getLanguage());
    entity.setTags(dto.getTags());
    entity.setUpdateDate(new Date());
    entity = groupRepo.save(entity);
    int wordCount = (int) wordRepo.countByGroupIdAndStatus(id, STATUS_ACTIVE);
    return toGroupDto(entity, wordCount);
  }

  @Override
  @Transactional
  public void deleteGroup(String username, Long id) {
    groupRepo.findByIdAndUsername(id, username)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    wordRepo.deleteAllByGroupId(id);
    groupRepo.deleteById(id);
  }

  // ── Words ─────────────────────────────────────────────────────────────────

  @Override
  public List<UserCustomWordDto> getWords(String username, Long groupId) {
    // Verify the group belongs to this user
    groupRepo.findByIdAndUsername(groupId, username)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    return wordRepo.findAllByGroupIdAndStatus(groupId, STATUS_ACTIVE).stream()
        .map(this::toWordDto)
        .collect(Collectors.toList());
  }

  @Override
  public UserCustomWordDto addWord(String username, Long groupId, UserCustomWordDto dto) {
    groupRepo.findByIdAndUsername(groupId, username)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    if (wordRepo.countByGroupIdAndStatus(groupId, STATUS_ACTIVE) >= MAX_WORDS_PER_GROUP) {
      throw HeloServiceException.of(ResponseCode.WORD_LIMIT_EXCEEDED);
    }
    UserCustomWordEntity entity = UserCustomWordEntity.builder()
        .groupId(groupId)
        .username(username)
        .word(dto.getWord())
        .translateEn(dto.getTranslateEn())
        .translateCh(dto.getTranslateCh())
        .sentence(dto.getSentence())
        .phonetics(dto.getPhonetics())
        .sourceWordId(dto.getSourceWordId())
        .sourceTableName(dto.getSourceTableName())
        // tableName is the stable reference used by RecordQuiz (answerId + answerTableName)
        .tableName("USER_CUSTOM_WORD")
        .status(STATUS_ACTIVE)
        .createDate(new Date())
        .updateDate(new Date())
        .build();
    entity = wordRepo.save(entity);
    return toWordDto(entity);
  }

  @Override
  @Transactional
  public List<UserCustomWordDto> batchAddWords(String username, Long groupId, List<UserCustomWordDto> dtos) {
    groupRepo.findByIdAndUsername(groupId, username)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    long currentCount = wordRepo.countByGroupIdAndStatus(groupId, STATUS_ACTIVE);
    if (currentCount + dtos.size() > MAX_WORDS_PER_GROUP) {
      throw HeloServiceException.of(ResponseCode.WORD_LIMIT_EXCEEDED);
    }
    Date now = new Date();
    List<UserCustomWordEntity> entities = dtos.stream().map(dto ->
        UserCustomWordEntity.builder()
            .groupId(groupId)
            .username(username)
            .word(dto.getWord())
            .translateEn(dto.getTranslateEn())
            .translateCh(dto.getTranslateCh())
            .sentence(dto.getSentence())
            .phonetics(dto.getPhonetics())
            .sourceWordId(dto.getSourceWordId())
            .sourceTableName(dto.getSourceTableName())
            .tableName("USER_CUSTOM_WORD")
            .status(STATUS_ACTIVE)
            .createDate(now)
            .updateDate(now)
            .build()
    ).collect(Collectors.toList());
    return wordRepo.saveAll(entities).stream().map(this::toWordDto).collect(Collectors.toList());
  }

  @Override
  public UserCustomWordDto updateWord(String username, Long wordId, UserCustomWordDto dto) {
    UserCustomWordEntity entity = wordRepo.findByIdAndUsername(wordId, username)
        .orElseThrow(() -> new IllegalArgumentException("Word not found"));
    entity.setWord(dto.getWord());
    entity.setTranslateEn(dto.getTranslateEn());
    entity.setTranslateCh(dto.getTranslateCh());
    entity.setSentence(dto.getSentence());
    entity.setPhonetics(dto.getPhonetics());
    entity.setUpdateDate(new Date());
    entity = wordRepo.save(entity);
    return toWordDto(entity);
  }

  @Override
  public void deleteWord(String username, Long wordId) {
    UserCustomWordEntity entity = wordRepo.findByIdAndUsername(wordId, username)
        .orElseThrow(() -> new IllegalArgumentException("Word not found"));
    wordRepo.delete(entity);
  }

  @Override
  @Transactional
  public void batchDeleteWords(String username, Long groupId, List<Long> wordIds) {
    if (wordIds == null || wordIds.isEmpty()) return;
    // Verify the group belongs to this user
    groupRepo.findByIdAndUsername(groupId, username)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    // Only delete words that belong to this user AND this group
    List<UserCustomWordEntity> entities =
        wordRepo.findAllByIdInAndUsernameAndGroupId(wordIds, username, groupId);
    wordRepo.deleteAll(entities);
  }

  // ── Mappers ───────────────────────────────────────────────────────────────

  private UserCustomGroupDto toGroupDto(UserCustomGroupEntity e, int wordCount) {
    UserCustomGroupDto dto = new UserCustomGroupDto();
    dto.setId(e.getId());
    dto.setName(e.getName());
    dto.setDescription(e.getDescription());
    dto.setLanguage(e.getLanguage());
    dto.setTags(e.getTags());
    dto.setWordCount(wordCount);
    dto.setCreateDate(e.getCreateDate());
    return dto;
  }

  private UserCustomWordDto toWordDto(UserCustomWordEntity e) {
    UserCustomWordDto dto = new UserCustomWordDto();
    dto.setId(e.getId());
    dto.setGroupId(e.getGroupId());
    dto.setWord(e.getWord());
    dto.setTranslateEn(e.getTranslateEn());
    dto.setTranslateCh(e.getTranslateCh());
    dto.setSentence(e.getSentence());
    dto.setPhonetics(e.getPhonetics());
    dto.setSourceWordId(e.getSourceWordId());
    dto.setSourceTableName(e.getSourceTableName());
    return dto;
  }
}
