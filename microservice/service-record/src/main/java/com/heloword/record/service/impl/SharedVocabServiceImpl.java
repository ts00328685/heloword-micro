package com.heloword.record.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.heloword.common.entity.vocab.SharedVocabGroupEntity;
import com.heloword.common.entity.vocab.UserCustomGroupEntity;
import com.heloword.common.entity.vocab.UserCustomWordEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.model.dto.SharedVocabGroupDto;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.common.repo.vocab.SharedVocabGroupRepository;
import com.heloword.common.repo.vocab.UserCustomGroupRepository;
import com.heloword.common.repo.vocab.UserCustomWordRepository;
import com.heloword.common.type.ResponseCode;
import com.heloword.record.service.SharedVocabService;

@Slf4j
@Service
public class SharedVocabServiceImpl implements SharedVocabService {

  private static final int STATUS_ACTIVE = 1;
  private static final String STATUS_PENDING = "PENDING";
  private static final String STATUS_APPROVED = "APPROVED";
  private static final String STATUS_REJECTED = "REJECTED";
  private static final int MAX_GROUPS_PER_USER = 30;

  @Autowired
  private SharedVocabGroupRepository sharedRepo;

  @Autowired
  private UserCustomGroupRepository groupRepo;

  @Autowired
  private UserCustomWordRepository wordRepo;

  @Override
  public SharedVocabGroupDto requestShare(String requesterUsername, String requesterUuid,
      String requesterDisplayName, Long groupId) {
    // Verify the group exists and belongs to the requester
    UserCustomGroupEntity group = groupRepo.findByIdAndUsername(groupId, requesterUsername)
        .orElseThrow(() -> new IllegalArgumentException("Group not found"));

    // Check for an existing request
    Optional<SharedVocabGroupEntity> existing =
        sharedRepo.findByRequesterUsernameAndGroupIdAndStatus(requesterUsername, groupId, STATUS_ACTIVE);
    if (existing.isPresent()) {
      return toDto(existing.get());
    }

    int wordCount = (int) wordRepo.countByGroupIdAndStatus(groupId, STATUS_ACTIVE);

    SharedVocabGroupEntity entity = SharedVocabGroupEntity.builder()
        .groupId(groupId)
        .requesterUsername(requesterUsername)
        .requesterUuid(requesterUuid)
        .requesterDisplayName(requesterDisplayName)
        .name(group.getName())
        .description(group.getDescription())
        .language(group.getLanguage())
        .tags(group.getTags())
        .wordCount(wordCount)
        .shareStatus(STATUS_PENDING)
        .status(STATUS_ACTIVE)
        .createDate(new Date())
        .updateDate(new Date())
        .build();

    entity = sharedRepo.save(entity);
    return toDto(entity);
  }

  @Override
  public Optional<SharedVocabGroupDto> getShareStatus(String requesterUsername, Long groupId) {
    return sharedRepo.findByRequesterUsernameAndGroupIdAndStatus(requesterUsername, groupId, STATUS_ACTIVE)
        .map(this::toDto);
  }

  @Override
  public List<SharedVocabGroupDto> getApprovedGroups() {
    return sharedRepo.findAllByShareStatusAndStatus(STATUS_APPROVED, STATUS_ACTIVE).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<UserCustomWordDto> getGroupWords(Long shareId) {
    SharedVocabGroupEntity shared = sharedRepo.findByIdAndStatus(shareId, STATUS_ACTIVE)
        .filter(e -> STATUS_APPROVED.equals(e.getShareStatus()))
        .orElseThrow(() -> new IllegalArgumentException("Shared group not found or not approved"));

    return wordRepo.findAllByGroupIdAndStatus(shared.getGroupId(), STATUS_ACTIVE).stream()
        .map(this::toWordDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<SharedVocabGroupDto> getPendingRequests() {
    return sharedRepo.findAllByShareStatusAndStatus(STATUS_PENDING, STATUS_ACTIVE).stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public void approve(Long shareId) {
    SharedVocabGroupEntity entity = sharedRepo.findByIdAndStatus(shareId, STATUS_ACTIVE)
        .orElseThrow(() -> new IllegalArgumentException("Share request not found"));
    entity.setShareStatus(STATUS_APPROVED);
    entity.setUpdateDate(new Date());
    sharedRepo.save(entity);
  }

  @Override
  public void reject(Long shareId) {
    SharedVocabGroupEntity entity = sharedRepo.findByIdAndStatus(shareId, STATUS_ACTIVE)
        .orElseThrow(() -> new IllegalArgumentException("Share request not found"));
    entity.setShareStatus(STATUS_REJECTED);
    entity.setUpdateDate(new Date());
    sharedRepo.save(entity);
  }

  @Override
  public void deleteSharedGroup(Long shareId) {
    SharedVocabGroupEntity entity = sharedRepo.findByIdAndStatus(shareId, STATUS_ACTIVE)
        .orElseThrow(() -> new IllegalArgumentException("Shared group not found"));
    entity.setStatus(0);
    entity.setUpdateDate(new Date());
    sharedRepo.save(entity);
  }

  @Override
  @Transactional
  public UserCustomGroupDto copyToUser(String targetUsername, Long shareId) {
    SharedVocabGroupEntity shared = sharedRepo.findByIdAndStatus(shareId, STATUS_ACTIVE)
        .filter(e -> STATUS_APPROVED.equals(e.getShareStatus()))
        .orElseThrow(() -> new IllegalArgumentException("Shared group not found or not approved"));

    if (groupRepo.countByUsernameAndStatus(targetUsername, STATUS_ACTIVE) >= MAX_GROUPS_PER_USER) {
      throw HeloServiceException.of(ResponseCode.GROUP_LIMIT_EXCEEDED);
    }

    // Create new group for target user
    UserCustomGroupEntity newGroup = UserCustomGroupEntity.builder()
        .username(targetUsername)
        .name(shared.getName())
        .description(shared.getDescription())
        .language(shared.getLanguage())
        .tags(shared.getTags())
        .status(STATUS_ACTIVE)
        .createDate(new Date())
        .updateDate(new Date())
        .build();
    newGroup = groupRepo.save(newGroup);

    // Copy all words
    final Long newGroupId = newGroup.getId();
    List<UserCustomWordEntity> sourceWords =
        wordRepo.findAllByGroupIdAndStatus(shared.getGroupId(), STATUS_ACTIVE);
    for (UserCustomWordEntity src : sourceWords) {
      UserCustomWordEntity copy = UserCustomWordEntity.builder()
          .groupId(newGroupId)
          .username(targetUsername)
          .word(src.getWord())
          .translateEn(src.getTranslateEn())
          .translateCh(src.getTranslateCh())
          .sentence(src.getSentence())
          .phonetics(src.getPhonetics())
          .sourceWordId(src.getSourceWordId())
          .sourceTableName(src.getSourceTableName())
          .tableName("USER_CUSTOM_WORD")
          .status(STATUS_ACTIVE)
          .createDate(new Date())
          .updateDate(new Date())
          .build();
      wordRepo.save(copy);
    }

    int wordCount = sourceWords.size();
    UserCustomGroupDto dto = new UserCustomGroupDto();
    dto.setId(newGroup.getId());
    dto.setName(newGroup.getName());
    dto.setDescription(newGroup.getDescription());
    dto.setLanguage(newGroup.getLanguage());
    dto.setTags(newGroup.getTags());
    dto.setWordCount(wordCount);
    dto.setCreateDate(newGroup.getCreateDate());
    return dto;
  }

  // ── Mapper ────────────────────────────────────────────────────────────────

  private SharedVocabGroupDto toDto(SharedVocabGroupEntity e) {
    SharedVocabGroupDto dto = new SharedVocabGroupDto();
    dto.setId(e.getId());
    dto.setGroupId(e.getGroupId());
    dto.setName(e.getName());
    dto.setDescription(e.getDescription());
    dto.setLanguage(e.getLanguage());
    dto.setTags(e.getTags());
    dto.setWordCount(e.getWordCount());
    dto.setSharerUuid(e.getRequesterUuid());
    dto.setSharerDisplayName(e.getRequesterDisplayName());
    dto.setShareStatus(e.getShareStatus());
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
