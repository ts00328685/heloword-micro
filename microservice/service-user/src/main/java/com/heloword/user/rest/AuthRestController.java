package com.heloword.user.rest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.user.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthRestController {

  @Autowired
  AuthService authService;

  @PostMapping("/verify-google-id")
  public HeloResponse<?> verifyGoogleId(@RequestBody Map<String, ?> request) throws GeneralSecurityException, IOException {
    String idToken = request.get("idToken").toString();
    Optional<GoogleIdToken.Payload> userPayload = authService.verifyGoogleIdToken(idToken);
    return HeloResponse.successWithoutData();
  }

}
