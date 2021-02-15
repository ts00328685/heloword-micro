package com.heloword.word.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heloword.common.base.rest.BaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.WordEntity;

@RestController
@RequestMapping("/")
public class WordRestController extends BaseRestController<WordEntity, String> {
	
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
