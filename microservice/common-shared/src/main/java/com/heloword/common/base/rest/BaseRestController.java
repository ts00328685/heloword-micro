package com.heloword.common.base.rest;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.heloword.common.base.service.IBaseService;


/**
 * The Class BaseRestController.
 *
 * @param <T> the generic type
 * @param <ID> the generic type
 */
public abstract class BaseRestController<T, ID>{

	/**
	 * Gets the service.
	 *
	 * @return the service
	 */
	public abstract IBaseService<T, ID> getService();

	/**
	 * Creates the.
	 *
	 * @param obj the obj
	 * @return the response entity
	 * @throws Exception the exception
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/")
	public ResponseEntity create(@Valid @RequestBody T obj) throws Exception {
		return handleSuccess(getService().save(obj));
	}

	/**
	 * Find all.
	 *
	 * @return the response entity
	 * @throws Exception the exception
	 */
	@SuppressWarnings("rawtypes")
	@GetMapping("/")
	public ResponseEntity findAll() throws Exception {
		return handleSuccess(getService().findAll());
	}
	
	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the response entity
	 * @throws Exception the exception
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping("/findById")
	public ResponseEntity findById(@RequestBody ID id) throws Exception {
		return handleSuccess(getService().findById(id));
	}
	
	/**
	 * Update.
	 *
	 * @param obj the obj
	 * @return the response entity
	 * @throws Exception the exception
	 */
	@SuppressWarnings("rawtypes")
	@PutMapping("/")
	public ResponseEntity update(@Valid @RequestBody T obj) throws Exception {
		return handleSuccess(getService().save(obj));
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 * @return the response entity
	 * @throws Exception the exception
	 */
	@SuppressWarnings("rawtypes")
	@DeleteMapping("/")
	public ResponseEntity delete(@RequestBody ID id) throws Exception {
		getService().deleteById(id);
		return handleSuccess(deleteSuccess());
	}
	
	/**
	 * Handle success.
	 *
	 * @param <O> the generic type
	 * @param obj the obj
	 * @return the response entity
	 */
	protected <O> ResponseEntity<O> handleSuccess(O obj) {
		return ResponseEntity.ok().body(obj);
	}
	
	/**
	 * Delete success.
	 *
	 * @return the map
	 */
	protected Map<String, Boolean> deleteSuccess() {
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return response;
	}
	
	/**
	 * Process success.
	 *
	 * @return the map
	 */
	protected Map<String, String> success() {
		Map<String, String> response = new HashMap<>();
		response.put("message", "success");
		return response;
	}

}
