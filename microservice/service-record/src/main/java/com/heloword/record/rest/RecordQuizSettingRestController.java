package com.heloword.record.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
		RecordQuizSettingEntity condition = RecordQuizSettingEntity.builder().username(decodeHeader(username)).build();
		ExampleMatcher matcher = ExampleMatcher.matchingAny()
				.withMatcher("username", ExampleMatcher.GenericPropertyMatchers.ignoreCase().exact());
		return success(getService().findAll(Example.of(condition, matcher)));
	}

	@PostMapping("/delete-batch")
	public HeloResponse<?> deleteBatch(@RequestBody List<Long> ids) {
		recordQuizSettingService.deleteBatch(ids);
		return success();
	}

	@GetMapping("/get-finished-count")
	public HeloResponse<?> getQuizSettingFinishedCount(@RequestHeader String username) {
		return success(
				recordQuizSettingService.getQuizSettingFinishedCount(decodeHeader(username))
						.parallelStream()
						.collect(Collectors.toMap(
								k -> ((Number) k.get("id")).longValue(),
								v -> ((Number) v.get("finished_count")).longValue()
						))
		);
	}

	private static String decodeHeader(String value) {
		if (value == null) return null;
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			return value;
		}
	}

}
