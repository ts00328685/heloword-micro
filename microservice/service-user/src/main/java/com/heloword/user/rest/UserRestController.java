package com.heloword.user.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.UserEntity;

@RestController
@RequestMapping("/")
public class UserRestController extends AbstractBaseRestController<UserEntity, String> {
	
	@GetMapping("/get-user")
	public ResponseEntity<?> getUser() {
		return ResponseEntity.ok("test");
	}

	@Override
	public IBaseService<UserEntity, String> getService() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
