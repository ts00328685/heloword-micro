package com.heloword.frontendapi.service.vocab.impl;

import java.util.List;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.feignclient.ServiceRecordClient;
import com.heloword.common.feignclient.ServiceUserClient;
import com.heloword.common.model.dto.UserDto;
import com.heloword.common.model.dto.VocabShareRequestDto;
import com.heloword.frontendapi.service.social.SocialPushService;
import com.heloword.frontendapi.service.vocab.VocabShareFrontendService;

@Log4j2
@Service
@AllArgsConstructor
public class VocabShareFrontendServiceImpl implements VocabShareFrontendService {

  private static final Pattern UUID_PATTERN = Pattern.compile(
      "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}=*",
      Pattern.CASE_INSENSITIVE);

  private ServiceRecordClient serviceRecordClient;
  private ServiceUserClient serviceUserClient;
  private SocialPushService socialPushService;

  @Override
  public VocabShareRequestDto sendShare(UserDto user, VocabShareRequestDto dto) {
    dto.setFromUsername(user.getUsername());
    // Capture the original value (may be a UUID) before resolution — used for the push topic.
    // The frontend subscribes to /topic/social/{uuid}/vocab-share so we must push to the UUID.
    String originalToId = dto.getToUsername();
    // If toUsername looks like a UUID (from the frontend UUID-based friend list),
    // resolve it to the actual email so service-record stores the real username.
    String resolved = resolveToUsername(originalToId);
    if (resolved == null) {
      log.warn("sendShare — could not resolve toUsername={}", originalToId);
      resolved = originalToId; // fall back to whatever was sent
    }
    dto.setToUsername(resolved);
    VocabShareRequestDto saved = serviceRecordClient.sendVocabShare(user.getUsername(), dto).getData();
    // Push real-time notification to the UUID-based topic the frontend subscribes on
    try {
      socialPushService.sendVocabShareToUser(originalToId, saved);
    } catch (Exception e) {
      log.warn("Failed to push vocab-share notification: {}", e.getMessage());
    }
    return maskUsernames(saved);
  }

  /** Replace email-based fromUsername/toUsername with UUIDs so the frontend never sees PII. */
  private VocabShareRequestDto maskUsernames(VocabShareRequestDto dto) {
    if (dto == null) return null;
    dto.setFromUsername(resolveEmailToUuid(dto.getFromUsername()));
    dto.setToUsername(resolveEmailToUuid(dto.getToUsername()));
    return dto;
  }

  /** Resolve an email to the member's UUID. Falls back to the original value on failure. */
  private String resolveEmailToUuid(String email) {
    if (email == null) return null;
    try {
      MemberEntity member = serviceUserClient.getMemberByEmail(email).getData();
      if (member != null && member.getUuid() != null) return member.getUuid();
    } catch (Exception e) {
      log.warn("resolveEmailToUuid — lookup failed for email={}: {}", email, e.getMessage());
    }
    return email;
  }

  /** If input is a UUID (with optional trailing '=' from Feign), resolve to email via user service. */
  private String resolveToUsername(String input) {
    if (input == null) return null;
    String stripped = input.replaceAll("=+$", "");
    if (!UUID_PATTERN.matcher(stripped).matches()) return input; // already an email
    try {
      MemberEntity member = serviceUserClient.getMemberByUuid(stripped).getData();
      if (member != null && member.getUsername() != null) {
        log.debug("resolveToUsername — resolved uuid={} to username={}", stripped, member.getUsername());
        return member.getUsername();
      }
      log.warn("resolveToUsername — uuid={} not found", stripped);
    } catch (Exception e) {
      log.error("resolveToUsername — lookup failed for uuid={}: {}", stripped, e.getMessage());
    }
    return null;
  }

  @Override
  public List<VocabShareRequestDto> getInbox(UserDto user) {
    List<VocabShareRequestDto> items = serviceRecordClient.getVocabShareInbox(user.getUsername()).getData();
    if (items == null) return java.util.Collections.emptyList();
    items.forEach(this::maskUsernames);
    return items;
  }

  @Override
  public VocabShareRequestDto acceptShare(UserDto user, Long id) {
    return maskUsernames(serviceRecordClient.acceptVocabShare(user.getUsername(), id).getData());
  }

  @Override
  public void rejectShare(UserDto user, Long id) {
    serviceRecordClient.rejectVocabShare(user.getUsername(), id);
  }
}
