package com.heloword.common.listener;

import java.time.Instant;
import java.util.Date;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import com.heloword.common.entity.BaseEntity;

public class BaseEntityListener {

  @PrePersist
  void onPrePersist(BaseEntity baseEntity) {
    baseEntity.setCreateDate(Date.from(Instant.now()));
    baseEntity.setVersion(0);
  }

  @PreUpdate
  void onPreUpdate(BaseEntity baseEntity) {
    baseEntity.setUpdateDate(Date.from(Instant.now()));
    baseEntity.setVersion(baseEntity.getVersion() + 1);
  }
}
