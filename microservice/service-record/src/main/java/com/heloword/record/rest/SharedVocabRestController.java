package com.heloword.record.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.model.dto.SharedVocabGroupDto;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.record.service.SharedVocabService;

@Log4j2
@RestController
@RequestMapping("/shared-vocab")
public class SharedVocabRestController {

  @Autowired
  private SharedVocabService sharedVocabService;

  private String decode(String value) {
    try { return URLDecoder.decode(value, StandardCharsets.UTF_8); } catch (Exception e) { return value; }
  }

  /** Public — all approved shared groups */
  @GetMapping("/groups")
  public HeloResponse<List<SharedVocabGroupDto>> getSharedGroups() {
    return HeloResponse.successWithData(sharedVocabService.getApprovedGroups());
  }

  /** Public — words in an approved shared group */
  @GetMapping("/groups/{shareId}/words")
  public HeloResponse<List<UserCustomWordDto>> getSharedGroupWords(@PathVariable Long shareId) {
    return HeloResponse.successWithData(sharedVocabService.getGroupWords(shareId));
  }

  /** Authenticated — request to make a group public */
  @PostMapping("/request")
  public HeloResponse<SharedVocabGroupDto> requestPublicShare(
      @RequestHeader String username,
      @RequestHeader String requesterUuid,
      @RequestHeader String requesterDisplayName,
      @RequestBody SharedVocabGroupDto dto) {
    return HeloResponse.successWithData(
        sharedVocabService.requestShare(
            decode(username),
            decode(requesterUuid),
            decode(requesterDisplayName),
            dto.getGroupId()));
  }

  /** Authenticated — check public share status of own group */
  @GetMapping("/status/{groupId}")
  public HeloResponse<SharedVocabGroupDto> getShareStatus(
      @RequestHeader String username,
      @PathVariable Long groupId) {
    return HeloResponse.successWithData(
        sharedVocabService.getShareStatus(decode(username), groupId).orElse(null));
  }

  /** Admin — get all pending requests */
  @GetMapping("/admin/pending")
  public HeloResponse<List<SharedVocabGroupDto>> getPendingRequests() {
    return HeloResponse.successWithData(sharedVocabService.getPendingRequests());
  }

  /** Admin — approve a share request */
  @PostMapping("/admin/{shareId}/approve")
  public HeloResponse<?> approve(@PathVariable Long shareId) {
    sharedVocabService.approve(shareId);
    return HeloResponse.successWithoutData();
  }

  /** Admin — reject a share request */
  @PostMapping("/admin/{shareId}/reject")
  public HeloResponse<?> reject(@PathVariable Long shareId) {
    sharedVocabService.reject(shareId);
    return HeloResponse.successWithoutData();
  }

  /** Admin — soft-delete a shared group (approved or pending) */
  @DeleteMapping("/admin/{shareId}")
  public HeloResponse<?> deleteSharedGroup(@PathVariable Long shareId) {
    sharedVocabService.deleteSharedGroup(shareId);
    return HeloResponse.successWithoutData();
  }

  /** Authenticated — copy an approved shared group to the user's own library */
  @PostMapping("/groups/{shareId}/copy")
  public HeloResponse<UserCustomGroupDto> copySharedGroup(
      @RequestHeader String username,
      @PathVariable Long shareId) {
    return HeloResponse.successWithData(sharedVocabService.copyToUser(decode(username), shareId));
  }
}
