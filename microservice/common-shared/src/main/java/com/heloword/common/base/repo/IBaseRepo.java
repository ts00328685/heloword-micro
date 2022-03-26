package com.heloword.common.base.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IBaseRepo<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
