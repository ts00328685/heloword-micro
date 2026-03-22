package com.heloword.frontendapi.service.challenge;

import java.util.List;
import com.heloword.frontendapi.model.challenge.ChallengeAnswerDto;
import com.heloword.frontendapi.model.challenge.ChallengeRoomDto;
import com.heloword.frontendapi.model.challenge.CreateRoomRequest;
import com.heloword.frontendapi.model.challenge.JoinRoomRequest;

public interface ChallengeService {
  List<ChallengeRoomDto> listRooms();
  ChallengeRoomDto createRoom(String hostUserId, String hostDisplayName, CreateRoomRequest req);
  ChallengeRoomDto joinRoom(String roomId, JoinRoomRequest req);
  void leaveRoom(String roomId, String userId);
  void startGame(String roomId, String requestingUserId);
  void processAnswer(String roomId, ChallengeAnswerDto answer);
}
