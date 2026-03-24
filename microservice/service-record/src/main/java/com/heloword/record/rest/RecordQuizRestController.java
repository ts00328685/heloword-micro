package com.heloword.record.rest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.rest.AbstractBaseRestController;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.entity.record.RecordQuizEntity;
import com.heloword.record.service.RecordQuizService;

@RestController()
@RequestMapping("/record-quiz")
public class RecordQuizRestController extends AbstractBaseRestController<RecordQuizEntity, Long> {

	@Autowired
	private RecordQuizService recordQuizService;

	@Override
	public IBaseService<RecordQuizEntity, Long> getService() {
		return recordQuizService;
	}

	@PostMapping("/delete-by-setting-ids")
	@Transactional
	public HeloResponse<?> deleteBySettingIds(@RequestBody List<Long> settingIds) {
		recordQuizService.deleteBySettingIds(settingIds);
		return success();
	}

	@PostMapping("/get-by-setting-ids")
	public HeloResponse<?> getAllRecordsBySettingIds(@RequestBody List<Long> settingIds, @RequestHeader String username) {
		return success(recordQuizService.getAllRecordsBySettingIds(settingIds, decodeHeader(username)));
	}

	@PostMapping("/get-latest-finished-time-by-setting-ids")
	public HeloResponse<?> getLatestUpdateTimeBySettingIds(@RequestBody List<Long> settingIds, @RequestHeader String username) {
		return success(recordQuizService.getLatestFinishedTimeBySettingIds(settingIds, decodeHeader(username)));
	}

	@GetMapping("/get-by-date-range")
	public HeloResponse<?> getRecordsByDateRange(@RequestHeader String username, @RequestParam long from, @RequestParam long to) {
		return success(recordQuizService.getRecordsByDateRange(decodeHeader(username), new Date(from), new Date(to)));
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
