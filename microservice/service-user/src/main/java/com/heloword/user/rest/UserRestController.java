package com.heloword.user.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.MemberEntity;
import com.heloword.user.service.MemberService;

@RestController
@RequestMapping("/user")
public class UserRestController extends AbstractBaseRestController<MemberEntity, Long> {

  @Autowired
  MemberService memberService;

  @Override
  protected IBaseService<MemberEntity, Long> getService() {
    return memberService;
  }
}