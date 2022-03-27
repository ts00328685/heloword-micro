package com.heloword.common.repo;

import java.util.Optional;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.entity.MemberEntity;

public interface MemberRepository extends IBaseRepo<MemberEntity, Long> {
  Optional<MemberEntity> findByEmail(String email);
}
