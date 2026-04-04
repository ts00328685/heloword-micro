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
import com.heloword.common.entity.vocab.VocabShareRequestEntity;
import com.heloword.common.model.dto.VocabShareRequestDto;
import com.heloword.common.repo.vocab.UserCustomGroupRepository;
import com.heloword.common.repo.vocab.UserCustomWordRepository;
import com.heloword.common.repo.vocab.VocabShareRequestRepository;
import com.heloword.record.service.VocabShareService;

@Slf4j
@Service
public class VocabShareServiceImpl implements VocabShareService {

  private static final int STATUS_ACTIVE = 1;
  private static final String PENDING  = "PENDING";
  private static final String ACCEPTED = "ACCEPTED";
  private static final String REJECTED = "REJECTED";

  @Autowired private VocabShareRequestRepository shareRepo;
  @Autowired private UserCustomGroupRepository groupRepo;
  @Autowired private UserCustomWordRepository wordRepo;

  @Override
  @Transactional
  public VocabShareRequestDto sendShare(String fromUsername, VocabShareRequestDto dto) {
    UserCustomGroupEntity group = groupRepo
        .findByIdAndUsername(dto.getSourceGroupId(), fromUsername)
        .orElseThrow(() -> new RuntimeException("Group not found"));

    int wordCount = (int) wordRepo.countByGroupIdAndStatus(group.getId(), STATUS_ACTIVE);

    VocabShareRequestEntity entity = VocabShareRequestEntity.builder()
        .fromUsername(fromUsername)
        .toUsername(dto.getToUsername())
        .sourceGroupId(group.getId())
        .groupName(group.getName())
        .description(group.getDescription())
        .language(group.getLanguage())
        .wordCount(wordCount)
        .shareStatus(PENDING)
        .status(STATUS_ACTIVE)
        .createDate(new Date())
        .updateDate(new Date())
        .build();

    VocabShareRequestEntity saved = shareRepo.save(entity);
    return toDto(saved);
  }

  @Override
  public List<VocabShareRequestDto> getInbox(String toUsername) {
    return shareRepo
        .findAllByToUsernameAndShareStatusAndStatus(toUsername, PENDING, STATUS_ACTIVE)
        .stream().map(this::toDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public VocabShareRequestDto acceptShare(Long id, String toUsername) {
    VocabShareRequestEntity share = shareRepo
        .findByIdAndToUsernameAndStatus(id, toUsername, STATUS_ACTIVE)
        .orElseThrow(() -> new RuntimeException("Share request not found"));

    // Create a new group for the recipient
    UserCustomGroupEntity newGroup = UserCustomGroupEntity.builder()
        .username(toUsername)
        .name(share.getGroupName())
        .description(share.getDescription())
        .language(share.getLanguage())
        .status(STATUS_ACTIVE)
        .createDate(new Date())
        .updateDate(new Date())
        .build();
    newGroup = groupRepo.save(newGroup);

    // Copy words from the sender's original group (best-effort — group may have changed)
    List<UserCustomWordEntity> sourceWords = wordRepo
        .findAllByGroupIdAndStatus(share.getSourceGroupId(), STATUS_ACTIVE);

    final Long newGroupId = newGroup.getId();
    List<UserCustomWordEntity> copies = sourceWords.stream().map(w ->
        UserCustomWordEntity.builder()
            .groupId(newGroupId)
            .username(toUsername)
            .word(w.getWord())
            .translateEn(w.getTranslateEn())
            .translateCh(w.getTranslateCh())
            .sentence(w.getSentence())
            .phonetics(w.getPhonetics())
            .sourceWordId(w.getId())
            .sourceTableName("USER_CUSTOM_WORD")
            .status(STATUS_ACTIVE)
            .createDate(new Date())
            .updateDate(new Date())
            .build()
    ).collect(Collectors.toList());
    wordRepo.saveAll(copies);

    share.setShareStatus(ACCEPTED);
    share.setUpdateDate(new Date());
    shareRepo.save(share);

    return toDto(share);
  }

  @Override
  @Transactional
  public void rejectShare(Long id, String toUsername) {
    VocabShareRequestEntity share = shareRepo
        .findByIdAndToUsernameAndStatus(id, toUsername, STATUS_ACTIVE)
        .orElseThrow(() -> new RuntimeException("Share request not found"));
    share.setShareStatus(REJECTED);
    share.setUpdateDate(new Date());
    shareRepo.save(share);
  }

  private VocabShareRequestDto toDto(VocabShareRequestEntity e) {
    VocabShareRequestDto dto = new VocabShareRequestDto();
    dto.setId(e.getId());
    dto.setFromUsername(e.getFromUsername());
    dto.setToUsername(e.getToUsername());
    dto.setSourceGroupId(e.getSourceGroupId());
    dto.setGroupName(e.getGroupName());
    dto.setDescription(e.getDescription());
    dto.setLanguage(e.getLanguage());
    dto.setWordCount(e.getWordCount());
    dto.setShareStatus(e.getShareStatus());
    dto.setCreateDate(e.getCreateDate());
    return dto;
  }
}
