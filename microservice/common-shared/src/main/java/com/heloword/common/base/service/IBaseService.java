package com.heloword.common.base.service;


import java.util.List;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import com.heloword.common.base.dto.BaseSearchBean;


public interface IBaseService<T, ID> {

	public T save(T obj) throws Exception;
	
	public List<T> saveAll(List<T> objs) throws Exception;

	public T findById(ID id) throws Exception;

	public List<T> findByIds(List<ID> ids) throws Exception;
	
	public List<T> findAll() throws Exception;

	public List<T> findAll(Sort sort) throws Exception;

	public List<T> findAll(BaseSearchBean searchBean) throws Exception;

	public List<T> findAll(Specification<T> specification) throws Exception;

	public Page<T> search(BaseSearchBean searchBean) throws Exception;

	public void delete(T obj) throws Exception;

	public void deleteById(ID id) throws Exception;
	
	public void deleteAll(List<T> objs) throws Exception;
	
	public void flush() throws Exception;

}
