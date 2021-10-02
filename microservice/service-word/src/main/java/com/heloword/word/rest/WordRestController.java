package com.heloword.word.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.WordEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WordRestController extends AbstractBaseRestController<WordEntity, String> {

	@GetMapping("/get-word")
	public ResponseEntity<?> getUser() {
		return ResponseEntity.ok("test");
	}

	@Override
	public IBaseService<WordEntity, String> getService() {
		// TODO Auto-generated method stub
		return null;
	}

}
