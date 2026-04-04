package com.heloword.record.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.model.dto.VocabShareRequestDto;
import com.heloword.record.service.VocabShareService;

@Log4j2
@RestController
@RequestMapping("/vocab-share")
public class VocabShareRestController {

  @Autowired
  private VocabShareService vocabShareService;

  private String decode(String value) {
    try { return URLDecoder.decode(value, StandardCharsets.UTF_8); } catch (Exception e) { return value; }
  }

  @PostMapping
  public HeloResponse<VocabShareRequestDto> sendShare(
      @RequestHeader String username,
      @RequestBody VocabShareRequestDto dto) {
    return HeloResponse.successWithData(vocabShareService.sendShare(decode(username), dto));
  }

  @GetMapping("/inbox")
  public HeloResponse<List<VocabShareRequestDto>> getInbox(@RequestHeader String username) {
    return HeloResponse.successWithData(vocabShareService.getInbox(decode(username)));
  }

  @PostMapping("/{id}/accept")
  public HeloResponse<VocabShareRequestDto> acceptShare(
      @RequestHeader String username,
      @PathVariable Long id) {
    return HeloResponse.successWithData(vocabShareService.acceptShare(id, decode(username)));
  }

  @PostMapping("/{id}/reject")
  public HeloResponse<?> rejectShare(
      @RequestHeader String username,
      @PathVariable Long id) {
    vocabShareService.rejectShare(id, decode(username));
    return HeloResponse.successWithoutData();
  }
}
