package com.heloword.common.entity;

import java.util.Date;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import com.heloword.common.listener.BaseEntityListener;

import lombok.Data;

@Data
@EntityListeners(BaseEntityListener.class)
@MappedSuperclass
public class BaseEntity {

  @Id
  @GeneratedValue
  private Long id;
  private Date createDate;
  private Date updateDate;
  private Long createBy;
  private Integer status;
  private Integer version;

}
