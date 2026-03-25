package service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import com.google.gson.Gson;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.user.UserApplication;
import com.heloword.user.service.MemberService;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
public class ServiceTest {

  @Autowired
  MemberService memberService;

  @Autowired
  RedisTemplate<String, String> redisTemplate;

  @Test
  void testMember() {

  }

}
