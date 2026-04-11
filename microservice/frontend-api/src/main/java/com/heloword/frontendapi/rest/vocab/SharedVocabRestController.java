package com.heloword.frontendapi.rest.vocab;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.model.dto.SharedVocabGroupDto;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.common.model.dto.UserDto;
import com.heloword.frontendapi.service.vocab.SharedVocabFrontendService;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/shared-vocab")
@AllArgsConstructor
public class SharedVocabRestController extends AbstractBaseFrontendRestController {

  private SharedVocabFrontendService sharedVocabFrontendService;

  /** Public — anyone (including guests) can view approved shared groups */
  @GetMapping("/groups")
  @PreAuthorize("hasAnyAuthority('MEMBER', 'UNREGISTERED_MEMBER')")
  public HeloResponse<List<SharedVocabGroupDto>> getSharedGroups() {
    return HeloResponse.successWithData(sharedVocabFrontendService.getApprovedGroups());
  }

  /** Public — anyone can read words in an approved shared group */
  @GetMapping("/groups/{shareId}/words")
  @PreAuthorize("hasAnyAuthority('MEMBER', 'UNREGISTERED_MEMBER')")
  public HeloResponse<List<UserCustomWordDto>> getSharedGroupWords(@PathVariable Long shareId) {
    return HeloResponse.successWithData(sharedVocabFrontendService.getGroupWords(shareId));
  }

  /** Authenticated — request to make own group public */
  @PostMapping("/request")
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  public HeloResponse<SharedVocabGroupDto> requestPublicShare(@RequestBody SharedVocabGroupDto dto) {
    UserDto user = getUser().orElseThrow();
    return HeloResponse.successWithData(sharedVocabFrontendService.requestShare(user, dto.getGroupId()));
  }

  /** Authenticated — check share status for own group */
  @GetMapping("/status/{groupId}")
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  public HeloResponse<SharedVocabGroupDto> getShareStatus(@PathVariable Long groupId) {
    UserDto user = getUser().orElseThrow();
    return HeloResponse.successWithData(
        sharedVocabFrontendService.getShareStatus(user, groupId).orElse(null));
  }

  /** Admin — list all pending share requests */
  @GetMapping("/admin/pending")
  @PreAuthorize("hasAnyAuthority('ADMIN')")
  public HeloResponse<List<SharedVocabGroupDto>> getPendingRequests() {
    return HeloResponse.successWithData(sharedVocabFrontendService.getPendingRequests());
  }

  /** Admin — approve a share request */
  @PostMapping("/admin/{shareId}/approve")
  @PreAuthorize("hasAnyAuthority('ADMIN')")
  public HeloResponse<?> approve(@PathVariable Long shareId) {
    sharedVocabFrontendService.approve(shareId);
    return HeloResponse.successWithoutData();
  }

  /** Admin — reject a share request */
  @PostMapping("/admin/{shareId}/reject")
  @PreAuthorize("hasAnyAuthority('ADMIN')")
  public HeloResponse<?> reject(@PathVariable Long shareId) {
    sharedVocabFrontendService.reject(shareId);
    return HeloResponse.successWithoutData();
  }

  /** Admin — delete a shared group (approved or pending) */
  @DeleteMapping("/admin/{shareId}")
  @PreAuthorize("hasAnyAuthority('ADMIN')")
  public HeloResponse<?> deleteSharedGroup(@PathVariable Long shareId) {
    sharedVocabFrontendService.deleteSharedGroup(shareId);
    return HeloResponse.successWithoutData();
  }

  /** Authenticated — copy an approved shared group to user's own library */
  @PostMapping("/groups/{shareId}/copy")
  @PreAuthorize("hasAnyAuthority('MEMBER')")
  public HeloResponse<UserCustomGroupDto> copySharedGroup(@PathVariable Long shareId) {
    UserDto user = getUser().orElseThrow();
    return HeloResponse.successWithData(sharedVocabFrontendService.copyToUser(user, shareId));
  }
}
