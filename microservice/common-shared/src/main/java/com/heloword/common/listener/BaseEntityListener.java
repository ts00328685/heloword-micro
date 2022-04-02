package com.heloword.common.listener;

import java.time.Instant;
import java.util.Date;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import com.heloword.common.base.entity.BaseEntity;

public class BaseEntityListener {

  @PrePersist
  void onPrePersist(BaseEntity baseEntity) {
    baseEntity.setCreateDate(Date.from(Instant.now()));
    baseEntity.setStatus(1);
    baseEntity.setVersion(0);
  }

  @PreUpdate
  void onPreUpdate(BaseEntity baseEntity) {
    baseEntity.setUpdateDate(Date.from(Instant.now()));
    if (baseEntity.getVersion() != null) {
      baseEntity.setVersion(baseEntity.getVersion() + 1);
    }
  }
}
