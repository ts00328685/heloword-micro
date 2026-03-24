package com.heloword.record.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.record.RecordQuizGroupOverrideEntity;
import com.heloword.common.repo.record.RecordQuizGroupOverrideRepository;

@RestController
@RequestMapping("/record-quiz-group-override")
public class RecordQuizGroupOverrideRestController {

  @Autowired
  private RecordQuizGroupOverrideRepository overrideRepository;

  @GetMapping("/by-username")
  public HeloResponse<List<RecordQuizGroupOverrideEntity>> getByUsername(@RequestHeader String username) {
    return HeloResponse.successWithData(overrideRepository.findAllByUsername(decode(username)));
  }

  @PostMapping("/save")
  @Transactional
  public HeloResponse<RecordQuizGroupOverrideEntity> save(
      @RequestHeader String username,
      @RequestBody RecordQuizGroupOverrideEntity entity) {
    String u = decode(username);
    // Upsert: delete existing override for same groupKey, then insert
    overrideRepository.findByUsernameAndGroupKey(u, entity.getGroupKey())
        .ifPresent(existing -> overrideRepository.deleteById(existing.getId()));

    entity.setUsername(u);
    if (entity.getSetAt() == null) entity.setSetAt(new Date());
    RecordQuizGroupOverrideEntity saved = overrideRepository.save(entity);
    return HeloResponse.successWithData(saved);
  }

  @DeleteMapping("/by-key")
  @Transactional
  public HeloResponse<?> deleteByKey(
      @RequestHeader String username,
      @RequestParam String groupKey) {
    overrideRepository.deleteByUsernameAndGroupKey(decode(username), groupKey);
    return HeloResponse.successWithoutData();
  }

  private static String decode(String value) {
    if (value == null) return null;
    try {
      return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    } catch (Exception e) {
      return value;
    }
  }
}
