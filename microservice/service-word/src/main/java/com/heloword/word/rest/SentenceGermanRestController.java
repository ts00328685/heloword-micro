package com.heloword.word.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.word.SentenceGermanEntity;
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

	@GetMapping("/example/{word}")
	public HeloResponse<?> findAllByExample(@PathVariable String word) {
		SentenceGermanEntity condition = SentenceGermanEntity.builder().sentence(word).translateEn(word).build();
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
						.withMatcher("sentence", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
						.withMatcher("translateEn", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains());
		return success(getService().findAll(Example.of(condition, matcher)));
	}
	
}
