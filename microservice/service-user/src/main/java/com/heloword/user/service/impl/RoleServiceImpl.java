package com.heloword.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.heloword.common.base.repo.IBaseRepo;
import com.heloword.common.base.service.AbstractBaseServiceImpl;
import com.heloword.common.entity.user.RoleEntity;
import com.heloword.common.repo.user.RoleRepository;
import com.heloword.user.service.RoleService;

@Service
public class RoleServiceImpl extends AbstractBaseServiceImpl<RoleEntity, Long> implements RoleService {

  @Autowired
  RoleRepository roleRepository;

  @Override
  protected IBaseRepo getRepo() {
    return roleRepository;
  }
}
