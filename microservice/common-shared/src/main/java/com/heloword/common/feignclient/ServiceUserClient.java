package com.heloword.common.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.entity.MemberEntity;
import com.heloword.common.entity.RoleEntity;
import com.heloword.common.filter.FeignClientInterceptor;

@FeignClient(name = "SERVICE-USER", url = "${feign.service-user.url:}", configuration = FeignClientInterceptor.class)
public interface ServiceUserClient {

  @GetMapping("/api/user/{id}")
  HeloResponse<MemberEntity> getMemberById(@PathVariable Long id);

  @GetMapping("/api/user/email/{email}")
  HeloResponse<MemberEntity> getMemberByEmail(@PathVariable String email);

  @GetMapping("/api/role/{id}")
  HeloResponse<RoleEntity> getRoleById(@PathVariable Long id);

  @PostMapping("/api/user")
  HeloResponse<MemberEntity> createOrUpdatMember(MemberEntity member);
}
