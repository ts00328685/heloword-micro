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
import com.heloword.common.entity.WordJapaneseEntity;
import com.heloword.word.service.WordJapaneseService;

@RestController()
@RequestMapping("/word-japanese")
public class WordJapaneseRestController extends AbstractBaseRestController<WordJapaneseEntity, Long> {

	@Autowired
	private WordJapaneseService wordJapaneseService;

	@Override
	public IBaseService<WordJapaneseEntity, Long> getService() {
		return wordJapaneseService;
	}

	@GetMapping("/example/{word}")
	public HeloResponse<?> findAllByExample(@PathVariable String word) {
		WordJapaneseEntity condition = WordJapaneseEntity.builder().word(word).translateEn(word).translateCh(word).build();
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("word", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
				.withMatcher("translateCn", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains())
				.withMatcher("translateEn", ExampleMatcher.GenericPropertyMatchers.ignoreCase().contains());
		return success(getService().findAll(Example.of(condition, matcher)));
	}
}
