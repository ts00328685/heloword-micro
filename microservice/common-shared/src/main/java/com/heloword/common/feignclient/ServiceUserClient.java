package com.heloword.common.feignclient;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.MemberEntity;

@FeignClient("SERVICE-USER")
public interface ServiceUserClient {

  @GetMapping("/api/user/{id}")
  HeloResponse<?> getMemberById(@PathVariable Long id);

  @PostMapping("/api/auth/verify-google-id")
  HeloResponse<MemberEntity> verifyGoogleId(@RequestBody Map<String, ?> request);

}
