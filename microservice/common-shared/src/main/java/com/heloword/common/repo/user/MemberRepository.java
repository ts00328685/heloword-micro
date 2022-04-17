package com.heloword.common.repo.user;

import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.user.MemberEntity;

public interface MemberRepository extends IBaseRepo<MemberEntity, Long> {
  MemberEntity findByEmail(String email);
}
