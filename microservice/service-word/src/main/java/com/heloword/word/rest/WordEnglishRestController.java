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
import com.heloword.common.entity.word.WordEnglishEntity;
import com.heloword.word.service.WordEnglishService;

@RestController()
@RequestMapping("/word-english")
public class WordEnglishRestController extends AbstractBaseRestController<WordEnglishEntity, Long> {

	@Autowired
	private WordEnglishService wordEnglishService;

	@Override
	public IBaseService<WordEnglishEntity, Long> getService() {
		return wordEnglishService;
	}

	@GetMapping("/example/{word}")
	public HeloResponse<?> findAllByExample(@PathVariable String word) {
		WordEnglishEntity condition = WordEnglishEntity.builder().word(word).translateEn(word).translateCh(word).build();
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("word", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
				.withMatcher("translateCn", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
				.withMatcher("translateEn", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains());
		return success(getService().findAll(Example.of(condition, matcher)));
	}
}
