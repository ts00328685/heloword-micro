package com.heloword.auth.rest;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.heloword.auth.service.AuthService;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.MemberEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;
import com.heloword.common.util.UserSessionUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthRestController {

  @Autowired
  AuthService authService;

  @Autowired
  UserSessionUtil userSessionUtil;

  @PostMapping("/verify-google-id")
  public HeloResponse<MemberEntity> verifyGoogleId(@RequestBody Map<String, ?> request) {
    String idToken = request.get("idToken").toString();

    Optional<GoogleIdToken.Payload> payload = authService.verifyGoogleIdToken(idToken);
    if (payload.isPresent()) {
      MemberEntity existingMember = authService.findOrRegisterUser(idToken, payload.get()).orElseThrow(() -> HeloServiceException.of(ResponseCode.MEMBER_FINDING_CREATION_ERROR));
      userSessionUtil.saveUserToSessionByKey(idToken, existingMember);
      return HeloResponse.successWithData(existingMember);
    } else {
      return HeloResponse.fail(ResponseCode.GOOGLE_VERIFICATION_ERROR);
    }
  }

}
