package com.heloword.common.base.service;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Sort;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.heloword.common.base.repo.IBaseRepo;

public abstract class AbstractBaseServiceImpl<T, ID> implements IBaseService<T, ID>  {

  @Override
  public T save(T obj) {
    return getRepo().save(obj);
  }

  @Override
  public List<T> saveAll(List<T> objs) {
    return getRepo().saveAll(objs);
  }

  @Override
  public Optional<T> findById(ID id) {
    return getRepo().findById(id);
  }

  @Override
  public List<T> findByIds(List<ID> ids) {
    return getRepo().findAllById(ids);
  }

  @Override
  public List<T> findAll(Example<T> example) {
    return getRepo().findAll(example);
  }

  @Override
  public List<T> findAll(Specification<T> specification) {
    return getRepo().findAll(specification);
  }

  @Override
  public Page<T> findAllPaged(Pageable pageable) {
    return getRepo().findAll(pageable);
  }

  @Override
  public List<T> findAll(Sort sort) {
    return getRepo().findAll();
  }

  @Override
  public List<T> findAll() {
    return getRepo().findAll();
  }

  @Override
  public void delete(T obj) {
    getRepo().delete(obj);
  }

  @Override
  public void deleteById(ID id) {
    getRepo().deleteById(id);
  }

  @Override
  public void deleteAll(List<T> objs) {
    getRepo().deleteAll(objs);
  }

  @Override
  public void flush() {
    getRepo().flush();
  }

  protected abstract IBaseRepo<T, ID> getRepo();
}
