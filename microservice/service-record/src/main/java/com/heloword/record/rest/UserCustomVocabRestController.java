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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;
import com.heloword.record.service.UserCustomVocabService;

@Log4j2
@RestController
@RequestMapping("/custom-vocab")
public class UserCustomVocabRestController {

  @Autowired
  private UserCustomVocabService vocabService;

  private String decode(String value) {
    try {
      return URLDecoder.decode(value, StandardCharsets.UTF_8);
    } catch (Exception e) {
      return value;
    }
  }

  // ── Groups ────────────────────────────────────────────────────────────────

  @GetMapping("/groups")
  public HeloResponse<List<UserCustomGroupDto>> getGroups(@RequestHeader String username) {
    return HeloResponse.successWithData(vocabService.getGroups(decode(username)));
  }

  @PostMapping("/groups")
  public HeloResponse<UserCustomGroupDto> createGroup(
      @RequestHeader String username,
      @RequestBody UserCustomGroupDto dto) {
    return HeloResponse.successWithData(vocabService.createGroup(decode(username), dto));
  }

  @PutMapping("/groups/{id}")
  public HeloResponse<UserCustomGroupDto> updateGroup(
      @RequestHeader String username,
      @PathVariable Long id,
      @RequestBody UserCustomGroupDto dto) {
    return HeloResponse.successWithData(vocabService.updateGroup(decode(username), id, dto));
  }

  @DeleteMapping("/groups/{id}")
  public HeloResponse<?> deleteGroup(
      @RequestHeader String username,
      @PathVariable Long id) {
    vocabService.deleteGroup(decode(username), id);
    return HeloResponse.successWithoutData();
  }

  // ── Words ─────────────────────────────────────────────────────────────────

  @GetMapping("/groups/{id}/words")
  public HeloResponse<List<UserCustomWordDto>> getWords(
      @RequestHeader String username,
      @PathVariable Long id) {
    return HeloResponse.successWithData(vocabService.getWords(decode(username), id));
  }

  @PostMapping("/groups/{id}/words")
  public HeloResponse<UserCustomWordDto> addWord(
      @RequestHeader String username,
      @PathVariable Long id,
      @RequestBody UserCustomWordDto dto) {
    return HeloResponse.successWithData(vocabService.addWord(decode(username), id, dto));
  }

  @PostMapping("/groups/{id}/words/batch")
  public HeloResponse<List<UserCustomWordDto>> batchAddWords(
      @RequestHeader String username,
      @PathVariable Long id,
      @RequestBody List<UserCustomWordDto> dtos) {
    return HeloResponse.successWithData(vocabService.batchAddWords(decode(username), id, dtos));
  }

  @PutMapping("/words/{wordId}")
  public HeloResponse<UserCustomWordDto> updateWord(
      @RequestHeader String username,
      @PathVariable Long wordId,
      @RequestBody UserCustomWordDto dto) {
    return HeloResponse.successWithData(vocabService.updateWord(decode(username), wordId, dto));
  }

  @DeleteMapping("/words/{wordId}")
  public HeloResponse<?> deleteWord(
      @RequestHeader String username,
      @PathVariable Long wordId) {
    vocabService.deleteWord(decode(username), wordId);
    return HeloResponse.successWithoutData();
  }

  @PostMapping("/groups/{id}/words/batch-delete")
  public HeloResponse<?> batchDeleteWords(
      @RequestHeader String username,
      @PathVariable Long id,
      @RequestBody List<Long> wordIds) {
    vocabService.batchDeleteWords(decode(username), id, wordIds);
    return HeloResponse.successWithoutData();
  }
}
