package com.heloword.user.service.impl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.heloword.common.entity.MemberEntity;
import com.heloword.common.repo.MemberRepository;
import com.heloword.common.repo.RoleRepository;
import com.heloword.user.service.AuthService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

  private static final String GOOGLE_CLIENT_ID = "268421074885-cn4qtlas4hep25tt7f0gaak8qh557fbu.apps.googleusercontent.com";

  @Autowired
  MemberRepository memberRepository;
  @Autowired
  RoleRepository roleRepository;

  @Override
  public Optional<GoogleIdToken.Payload> verifyGoogleIdToken(String idToken) throws GeneralSecurityException, IOException {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
        .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
        .build();
    return Optional.ofNullable(verifier.verify(idToken))
                    .map(GoogleIdToken::getPayload);
  }

  @Override
  public void createUserSession(GoogleIdToken.Payload idToken) {
    Optional<MemberEntity> member = memberRepository.findByEmail(idToken.getEmail());
    if (member.isPresent()) {
      MemberEntity memberEntity = member.get();
      memberEntity.setGoogleToken(idToken.getExpirationTimeSeconds().toString());
    } else {
      // create new member
      MemberEntity newMember = MemberEntity.builder()
          .email(idToken.getEmail())
          .username(idToken.getUserId())
          .googleToken(idToken.getExpirationTimeSeconds().toString())
          .status(1)
          .roles(new HashSet(Arrays.asList(roleRepository.findById(1L).get())))
          .build();
      memberRepository.save(newMember);
    }

  }
}
