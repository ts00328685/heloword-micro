package com.heloword.common.util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.heloword.common.entity.MemberEntity;

@Component
public class UserSessionUtil {

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @Autowired
  Gson gson;

  public void saveUserToSession(MemberEntity memberEntity) {
    redisTemplate.opsForValue().set(memberEntity.getEmail(), gson.toJson(memberEntity));
    redisTemplate.expire(memberEntity.getEmail(), 20, TimeUnit.SECONDS);
  }

  public Optional<MemberEntity> getUserFromSession(String email) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(email))
                   .map(userString -> gson.fromJson(userString, MemberEntity.class));
  }
}
