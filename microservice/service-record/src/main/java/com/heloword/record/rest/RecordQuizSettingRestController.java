package com.heloword.record.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
