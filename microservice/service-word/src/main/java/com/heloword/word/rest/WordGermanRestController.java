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
import com.heloword.common.entity.WordGermanEntity;
import com.heloword.word.service.WordGermanService;

@RestController()
@RequestMapping("/word-german")
public class WordGermanRestController extends AbstractBaseRestController<WordGermanEntity, Long> {

	@Autowired
	private WordGermanService wordGermanService;

	@Override
	public IBaseService<WordGermanEntity, Long> getService() {
		return wordGermanService;
	}

	@GetMapping("/example/{word}")
	public HeloResponse<?> findAllByExample(@PathVariable String word) {
		WordGermanEntity condition = WordGermanEntity.builder().word(word).translateEn(word).translateCh(word).build();
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("word", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
				.withMatcher("translateCn", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
				.withMatcher("translateEn", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains());
		return success(getService().findAll(Example.of(condition, matcher)));
	}
}
