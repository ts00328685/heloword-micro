package com.heloword.auth.service;

import java.util.Optional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.heloword.common.entity.MemberEntity;

public interface AuthService {
  Optional<GoogleIdToken.Payload> verifyGoogleIdToken(String idToken);
  Optional<MemberEntity> findOrRegisterUser(String idTokenKey, GoogleIdToken.Payload idToken);
}
