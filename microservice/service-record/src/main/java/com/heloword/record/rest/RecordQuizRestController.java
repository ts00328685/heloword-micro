package com.heloword.record.rest;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@PostMapping("/get-by-setting-ids")
	public HeloResponse<?> getAllRecordsBySettingIds(@RequestBody List<Long> settingIds, @RequestHeader String username) {
		return success(recordQuizService.getAllRecordsBySettingIds(settingIds, username));
	}
}
