package com.heloword.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.repo.user.MemberRepository;
import com.heloword.user.service.MemberService;

@Service
public class MemberServiceImpl extends AbstractBaseServiceImpl<MemberEntity, Long> implements MemberService {

  @Autowired
  MemberRepository memberRepository;

  @Override
  protected IBaseRepo getRepo() {
    return memberRepository;
  }

  public MemberEntity findByEmail(String email) {
    return memberRepository.findByEmail(email);
  }
}
