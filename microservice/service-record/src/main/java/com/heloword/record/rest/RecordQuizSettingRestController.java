package com.heloword.record.rest;

import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.record.RecordQuizSettingEntity;
import com.heloword.record.service.RecordQuizSettingService;

@RestController()
@RequestMapping("/record-quiz-setting")
public class RecordQuizSettingRestController extends AbstractBaseRestController<RecordQuizSettingEntity, Long> {

	@Autowired
	private RecordQuizSettingService recordQuizSettingService;

	@Override
	public IBaseService<RecordQuizSettingEntity, Long> getService() {
		return recordQuizSettingService;
	}

	@GetMapping("/get-quiz-settings")
	public HeloResponse<?> getQuizSettings(@RequestHeader String username) {
		RecordQuizSettingEntity condition = RecordQuizSettingEntity.builder().username(username).build();
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("username", ExampleMatcher.GenericPropertyMatchers.ignoreCase().exact());
		return success(getService().findAll(Example.of(condition, matcher)));
	}

	@GetMapping("/get-finished-count")
	public HeloResponse<?> getQuizSettingFinishedCount(@RequestHeader String username) {
		return success(
				recordQuizSettingService.getQuizSettingFinishedCount(username)
						.parallelStream()
						.collect(Collectors.toMap(k -> k.get("id"), v -> (BigInteger) v.get("finished_count")))
		);
	}

}
