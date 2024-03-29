package com.heloword.user.service;

import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.user.MemberEntity;

public interface MemberService extends IBaseService<MemberEntity, Long> {
  MemberEntity findByEmail(String email);
}
