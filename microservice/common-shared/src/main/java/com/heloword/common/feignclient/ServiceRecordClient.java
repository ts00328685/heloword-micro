package com.heloword.common.feignclient;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.common.entity.record.RecordQuizGroupOverrideEntity;
import com.heloword.common.entity.record.RecordQuizSettingEntity;
import com.heloword.common.entity.social.FriendEntity;
import com.heloword.common.filter.FeignClientInterceptor;
import com.heloword.common.model.dto.ChatMessageDto;
import com.heloword.common.model.dto.FriendDto;
import com.heloword.common.model.dto.UserCustomGroupDto;
import com.heloword.common.model.dto.UserCustomWordDto;

@FeignClient(name = "SERVICE-RECORD", url = "${feign.service-record.url:}", configuration = FeignClientInterceptor.class)
public interface ServiceRecordClient {

  @PostMapping("/api/record-quiz")
  HeloResponse<RecordQuizEntity> saveQuizRecord(RecordQuizEntity recordQuizEntity);

  @PostMapping("/api/record-quiz/get-by-setting-ids")
  HeloResponse<Map<Long, List<Long>>> getRecordIdsBySettingIds(@RequestHeader String username, @RequestBody List<Long> settingIds);

  @PostMapping("/api/record-quiz/get-latest-finished-time-by-setting-ids")
  HeloResponse<Map<Long, Date>> getLatestFinishedTimeBySettingIds(@RequestHeader String username, @RequestBody List<Long> settingIds);

  @PutMapping("/api/record-quiz-setting")
  HeloResponse<List<RecordQuizSettingEntity>> saveAllQuizSettingRecord(List<RecordQuizSettingEntity> recordQuizSettingEntity);

  @GetMapping("/api/record-quiz-setting/get-quiz-settings")
  HeloResponse<List<RecordQuizSettingEntity>> getQuizSettings(@RequestHeader String username);

  @GetMapping("/api/record-quiz-setting/get-finished-count")
  HeloResponse<Map<Long, Long>> getQuizSettingFinishedCount(@RequestHeader String username);

  @GetMapping("/api/record-quiz/get-by-date-range")
  HeloResponse<List<RecordQuizEntity>> getRecordsByDateRange(@RequestHeader String username, @RequestParam long from, @RequestParam long to);

  @PostMapping("/api/record-quiz-setting/delete-batch")
  HeloResponse<?> deleteSettingsByIds(@RequestBody List<Long> ids);

  @PostMapping("/api/record-quiz/delete-by-setting-ids")
  HeloResponse<?> deleteRecordsBySettingIds(@RequestBody List<Long> settingIds);

  // ── Group overrides ──────────────────────────────────────────────────────

  @GetMapping("/api/record-quiz-group-override/by-username")
  HeloResponse<List<RecordQuizGroupOverrideEntity>> getGroupOverrides(@RequestHeader String username);

  @PostMapping("/api/record-quiz-group-override/save")
  HeloResponse<RecordQuizGroupOverrideEntity> saveGroupOverride(@RequestHeader String username, @RequestBody RecordQuizGroupOverrideEntity entity);

  @DeleteMapping("/api/record-quiz-group-override/by-key")
  HeloResponse<?> deleteGroupOverride(@RequestHeader String username, @RequestParam String groupKey);

  // ── Social: Friends ──────────────────────────────────────────────────────

  @GetMapping("/api/social/friends")
  HeloResponse<List<FriendDto>> getFriends(@RequestHeader String username);

  @PostMapping("/api/social/friends/request")
  HeloResponse<FriendDto> sendFriendRequest(@RequestHeader String username, @RequestHeader String addresseeUsername);

  @PostMapping("/api/social/friends/accept/{id}")
  HeloResponse<FriendDto> acceptFriendRequest(@RequestHeader String username, @PathVariable Long id,
      @RequestHeader String addresseeDisplayName);

  @PostMapping("/api/social/friends/reject/{id}")
  HeloResponse<?> rejectFriendRequest(@RequestHeader String username, @PathVariable Long id);

  @DeleteMapping("/api/social/friends/{id}")
  HeloResponse<?> removeFriend(@RequestHeader String username, @PathVariable Long id);

  @PutMapping("/api/social/friends/{id}/nickname")
  HeloResponse<?> updateFriendNickname(@RequestHeader String username, @PathVariable Long id, @RequestHeader String nickname);

  // ── Social: Chat ─────────────────────────────────────────────────────────

  @PostMapping("/api/social/messages")
  HeloResponse<ChatMessageDto> sendMessage(@RequestBody ChatMessageDto chatMessageDto);

  @GetMapping("/api/social/messages/room/{roomId}")
  HeloResponse<List<ChatMessageDto>> getMessages(@PathVariable String roomId, @RequestParam(required = false) Long since);

  @PostMapping("/api/social/messages/read/{roomId}")
  HeloResponse<?> markRoomRead(@RequestHeader String recipientUserId, @PathVariable String roomId);

  @GetMapping("/api/social/messages/unread")
  HeloResponse<Map<String, Long>> getUnreadCounts(@RequestHeader String recipientUserId);

  @GetMapping("/api/social/messages/rooms")
  HeloResponse<List<ChatMessageDto>> getChatRooms(@RequestHeader String userId);

  // ── User Custom Vocabulary ────────────────────────────────────────────────

  @GetMapping("/api/custom-vocab/groups")
  HeloResponse<List<UserCustomGroupDto>> getCustomGroups(@RequestHeader String username);

  @PostMapping("/api/custom-vocab/groups")
  HeloResponse<UserCustomGroupDto> createCustomGroup(@RequestHeader String username, @RequestBody UserCustomGroupDto dto);

  @PutMapping("/api/custom-vocab/groups/{id}")
  HeloResponse<UserCustomGroupDto> updateCustomGroup(@RequestHeader String username, @PathVariable Long id, @RequestBody UserCustomGroupDto dto);

  @DeleteMapping("/api/custom-vocab/groups/{id}")
  HeloResponse<?> deleteCustomGroup(@RequestHeader String username, @PathVariable Long id);

  @GetMapping("/api/custom-vocab/groups/{id}/words")
  HeloResponse<List<UserCustomWordDto>> getCustomWords(@RequestHeader String username, @PathVariable Long id);

  @PostMapping("/api/custom-vocab/groups/{id}/words")
  HeloResponse<UserCustomWordDto> addCustomWord(@RequestHeader String username, @PathVariable Long id, @RequestBody UserCustomWordDto dto);

  @PutMapping("/api/custom-vocab/words/{wordId}")
  HeloResponse<UserCustomWordDto> updateCustomWord(@RequestHeader String username, @PathVariable Long wordId, @RequestBody UserCustomWordDto dto);

  @DeleteMapping("/api/custom-vocab/words/{wordId}")
  HeloResponse<?> deleteCustomWord(@RequestHeader String username, @PathVariable Long wordId);

}
