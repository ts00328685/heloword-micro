package com.heloword.common.base.service;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Sort;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IBaseService<T, ID> {

  T save(T obj);

  List<T> saveAll(List<T> objs);

  Optional<T> findById(ID id);

  List<T> findByIds(List<ID> ids);

  List<T> findAll();

  List<T> findAll(Sort sort);

  List<T> findAll(Example<T> example);

  List<T> findAll(Specification<T> specification);

  Page<T> findAllPaged(Pageable pageable);

  void delete(T obj);

  void deleteById(ID id);

  void deleteAll(List<T> objs);

  void flush();

}
