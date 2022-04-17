package com.heloword.auth.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.heloword.auth.service.AuthService;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.entity.user.RoleEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.feignclient.ServiceUserClient;
import com.heloword.common.type.ResponseCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

  private static final String GOOGLE_CLIENT_ID = "268421074885-cn4qtlas4hep25tt7f0gaak8qh557fbu.apps.googleusercontent.com";

  @Autowired
  ServiceUserClient serviceUserClient;

  @Override
  public Optional<GoogleIdToken.Payload> verifyGoogleIdToken(String idToken) {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
        .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
        .build();
    try {
      return Optional.ofNullable(verifier.verify(idToken)).map(GoogleIdToken::getPayload);
    } catch (Exception e) {
      log.error("verifying google id error", e);
      throw HeloServiceException.of(ResponseCode.GOOGLE_VERIFICATION_ERROR);
    }
  }

  @Override
  public Optional<MemberEntity> findOrRegisterUser(String idTokenKey, GoogleIdToken.Payload idToken) {
    log.debug("finding or registering user: {}", idToken);
    HeloResponse<MemberEntity> response = serviceUserClient.getMemberByEmail(idToken.getEmail());
    Optional<MemberEntity> member = Optional.ofNullable(response.getData());
    if (member.isPresent()) {
      log.debug("user already exists: {}", member);
      MemberEntity memberEntity = member.get();
      member = memberMapper(idTokenKey, idToken, memberEntity, null);
    } else {
      log.debug("creating new user");
      HashSet<RoleEntity> rolesForNewUser = new HashSet<>(Arrays.asList(serviceUserClient.getRoleById(1L).getData()));
      member = memberMapper(idTokenKey, idToken, null, rolesForNewUser);
    }
    HeloResponse<MemberEntity> memberCreatedOrUpdated = serviceUserClient.createOrUpdatMember(member.get());
    log.debug("user created or updated: {}", memberCreatedOrUpdated);
    return Optional.of(memberCreatedOrUpdated.getData());
  }

  private static Optional<MemberEntity> memberMapper(String idTokenKey, GoogleIdToken.Payload idToken, MemberEntity entity, Set<RoleEntity> roleEntitys) {
    if (entity != null) {
      entity.setFullname(idToken.get("name").toString());
      entity.setNickname(idToken.get("given_name").toString());
      entity.setPicture(idToken.get("picture").toString());
      entity.setGoogleToken(idToken.getUserId());
      return Optional.of(entity);
    } else {
      return Optional.of(MemberEntity.builder()
          .email(idToken.getEmail())
          .username(idToken.getEmail())
          .fullname(idToken.get("name").toString())
          .nickname(idToken.get("given_name").toString())
          .locale(idToken.get("locale").toString())
          .picture(idToken.get("picture").toString())
          .status(1)
          .roles(roleEntitys)
          .googleToken(idToken.getUserId())
          .build());
    }
  }
}
