package com.heloword.frontendapi.rest.vocab;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.frontendapi.service.vocab.UserCustomVocabFrontendService;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;

@Log4j2
@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/custom-vocab")
@AllArgsConstructor
@PreAuthorize("hasAnyAuthority('MEMBER')")
public class UserCustomVocabRestController extends AbstractBaseFrontendRestController {

  private UserCustomVocabFrontendService vocabFrontendService;

  // ── Groups ────────────────────────────────────────────────────────────────

  @GetMapping("/groups")
  public HeloResponse<List<UserCustomGroupDto>> getGroups() {
    return HeloResponse.successWithData(vocabFrontendService.getGroups(getUser().get()));
  }

  @PostMapping("/groups")
  public HeloResponse<UserCustomGroupDto> createGroup(@RequestBody UserCustomGroupDto dto) {
    return HeloResponse.successWithData(vocabFrontendService.createGroup(getUser().get(), dto));
  }

  @PutMapping("/groups/{id}")
  public HeloResponse<UserCustomGroupDto> updateGroup(
      @PathVariable Long id,
      @RequestBody UserCustomGroupDto dto) {
    return HeloResponse.successWithData(vocabFrontendService.updateGroup(getUser().get(), id, dto));
  }

  @DeleteMapping("/groups/{id}")
  public HeloResponse<?> deleteGroup(@PathVariable Long id) {
    vocabFrontendService.deleteGroup(getUser().get(), id);
    return HeloResponse.successWithoutData();
  }

  // ── Words ─────────────────────────────────────────────────────────────────

  @GetMapping("/groups/{id}/words")
  public HeloResponse<List<UserCustomWordDto>> getWords(@PathVariable Long id) {
    return HeloResponse.successWithData(vocabFrontendService.getWords(getUser().get(), id));
  }

  @PostMapping("/groups/{id}/words")
  public HeloResponse<UserCustomWordDto> addWord(
      @PathVariable Long id,
      @RequestBody UserCustomWordDto dto) {
    return HeloResponse.successWithData(vocabFrontendService.addWord(getUser().get(), id, dto));
  }

  @PostMapping("/groups/{id}/words/batch")
  public HeloResponse<List<UserCustomWordDto>> batchAddWords(
      @PathVariable Long id,
      @RequestBody List<UserCustomWordDto> dtos) {
    return HeloResponse.successWithData(vocabFrontendService.batchAddWords(getUser().get(), id, dtos));
  }

  @PutMapping("/words/{wordId}")
  public HeloResponse<UserCustomWordDto> updateWord(
      @PathVariable Long wordId,
      @RequestBody UserCustomWordDto dto) {
    return HeloResponse.successWithData(vocabFrontendService.updateWord(getUser().get(), wordId, dto));
  }

  @DeleteMapping("/words/{wordId}")
  public HeloResponse<?> deleteWord(@PathVariable Long wordId) {
    vocabFrontendService.deleteWord(getUser().get(), wordId);
    return HeloResponse.successWithoutData();
  }
}
