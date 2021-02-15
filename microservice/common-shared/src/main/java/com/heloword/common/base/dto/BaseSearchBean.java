package com.heloword.common.base.dto;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import lombok.Data;

@Data
public class BaseSearchBean<T> {

	private Integer size = 10;

	private Integer page = 0;

	private String orderBy;
	
	private String direction = "ASC";
	
	public Pageable getPageable() {
		if (StringUtils.isBlank(orderBy) || StringUtils.isBlank(direction)) {
			return PageRequest.of(page, size);
		} else {
			return PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(direction.toUpperCase()), orderBy));
		}
	}
	
	public Specification<T> getSpecification() {
		return (root, query, cb) -> {
			return query.getRestriction();
		};
	}
	
}
