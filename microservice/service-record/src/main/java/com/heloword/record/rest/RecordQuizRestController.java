package com.heloword.record.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
