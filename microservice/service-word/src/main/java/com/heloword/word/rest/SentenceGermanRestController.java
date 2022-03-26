package com.heloword.word.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.SentenceGermanEntity;
import com.heloword.word.service.SentenceGermanService;

@RestController()
@RequestMapping("/sentence-german")
public class SentenceGermanRestController extends AbstractBaseRestController<SentenceGermanEntity, Long> {

	@Autowired
	private SentenceGermanService sentenceGermanService;

	@Override
	public IBaseService<SentenceGermanEntity, Long> getService() {
		return sentenceGermanService;
	}

}
