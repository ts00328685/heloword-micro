package com.heloword.user.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.MemberEntity;
import com.heloword.common.entity.RoleEntity;
import com.heloword.user.service.MemberService;
import com.heloword.user.service.RoleService;

@RestController
@RequestMapping("/role")
public class RoleRestController extends AbstractBaseRestController<RoleEntity, Long> {

  @Autowired
  RoleService roleService;

  @Override
  protected IBaseService<RoleEntity, Long> getService() {
    return roleService;
  }
}
