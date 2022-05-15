package com.heloword.common.base.rest;

import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.heloword.common.base.dto.HeloResponse;
import com.heloword.common.base.service.IBaseService;
import com.heloword.common.type.ResponseCode;

public abstract class AbstractBaseRestController<T, ID>{

	protected abstract IBaseService<T, ID> getService();

	@PostMapping()
	public HeloResponse<?> save(@Valid @RequestBody T obj) {
		return success(getService().save(obj));
	}

	@GetMapping()
	public HeloResponse<?> findAll() {
		return success(getService().findAll());
	}

	@GetMapping("/{id}")
	public HeloResponse<?> findById(@PathVariable ID id) {
		return success(getService().findById(id).orElse(null));
	}

	@PutMapping()
	public HeloResponse<?> saveAll(@Valid @RequestBody List<T> objs) {
		return success(getService().saveAll(objs));
	}

	@DeleteMapping()
	public HeloResponse<?> delete(@RequestBody ID id) {
		getService().deleteById(id);
		return success();
	}

	protected HeloResponse<?> success() {
		return HeloResponse.successWithoutData();
	}

	protected HeloResponse<?> success(Object data) {
		return HeloResponse.successWithData(data);
	}

	protected HeloResponse<?> systemFailure() {
		return HeloResponse.fail(ResponseCode.SYSTEM_ERROR);
	}

	protected HeloResponse<?> fail(ResponseCode responseCode) {
		return HeloResponse.fail(responseCode);
	}

	protected HeloResponse<?> fail(String message) {
		return HeloResponse.fail(ResponseCode.SYSTEM_ERROR, message);
	}
}
