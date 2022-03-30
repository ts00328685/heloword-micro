package com.heloword.user.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.heloword.common.entity.MemberEntity;

public interface AuthService {
  Optional<GoogleIdToken.Payload> verifyGoogleIdToken(String idToken) throws GeneralSecurityException, IOException;
  Optional<MemberEntity> findOrRegisterUser(GoogleIdToken.Payload idToken);
}
