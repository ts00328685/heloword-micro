package com.heloword.common.util;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import com.google.gson.Gson;
import com.heloword.common.entity.MemberEntity;

@Component
public class UserSessionUtil {

  public static final int SESSION_EXPIRE_DAYS = 5;

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @Autowired
  Gson gson;

  public void saveUserToSessionByEmail(MemberEntity memberEntity) {
    redisTemplate.opsForValue().set(memberEntity.getEmail(), gson.toJson(memberEntity));
    redisTemplate.expire(memberEntity.getEmail(), SESSION_EXPIRE_DAYS, TimeUnit.DAYS);
  }

  public void saveUserToSessionByKey(String key, MemberEntity memberEntity) {
    redisTemplate.opsForValue().set(key, gson.toJson(memberEntity));
    redisTemplate.expire(key, SESSION_EXPIRE_DAYS, TimeUnit.DAYS);
  }

  public Optional<MemberEntity> getUserFromSession(String key) {
    if (StringUtils.isEmpty(key)) {
      return Optional.empty();
    }
    return Optional.ofNullable(redisTemplate.opsForValue().get(key))
                   .map(userString -> gson.fromJson(userString, MemberEntity.class));
  }
}
