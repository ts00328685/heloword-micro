package com.heloword.frontendapi.rest.challenge;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseFrontendRestController;
import static com.heloword.common.base.rest.AbstractBaseFrontendRestController.BASE_FRONTEND_API_URL;
import com.heloword.frontendapi.model.challenge.ChallengeRoomDto;
import com.heloword.frontendapi.model.challenge.CreateRoomRequest;
import com.heloword.frontendapi.model.challenge.JoinRoomRequest;
import com.heloword.frontendapi.service.challenge.ChallengeService;

@RestController
@RequestMapping(BASE_FRONTEND_API_URL + "/challenge")
@AllArgsConstructor
public class ChallengeRestController extends AbstractBaseFrontendRestController {

  private final ChallengeService challengeService;

  @GetMapping("/rooms")
  public HeloResponse<List<ChallengeRoomDto>> listRooms() {
    return HeloResponse.successWithData(challengeService.listRooms());
  }

  @PreAuthorize("hasAnyAuthority('MEMBER')")
  @PostMapping("/rooms")
  public HeloResponse<ChallengeRoomDto> createRoom(@RequestBody CreateRoomRequest req) {
    var user = getUser().get();
    return HeloResponse.successWithData(
        challengeService.createRoom(user.getUsername(),
            user.getNickname() != null ? user.getNickname() : user.getUsername(), req));
  }

  @PostMapping("/rooms/{roomId}/join")
  public HeloResponse<ChallengeRoomDto> joinRoom(@PathVariable String roomId,
      @RequestBody JoinRoomRequest req) {
    return HeloResponse.successWithData(challengeService.joinRoom(roomId, req));
  }

  @PostMapping("/rooms/{roomId}/leave")
  public HeloResponse<?> leaveRoom(@PathVariable String roomId, @RequestBody JoinRoomRequest req) {
    challengeService.leaveRoom(roomId, req.getUserId());
    return HeloResponse.successWithoutData();
  }

  @PostMapping("/rooms/{roomId}/start")
  public HeloResponse<?> startGame(@PathVariable String roomId, @RequestBody JoinRoomRequest req) {
    challengeService.startGame(roomId, req.getUserId());
    return HeloResponse.successWithoutData();
  }
}
