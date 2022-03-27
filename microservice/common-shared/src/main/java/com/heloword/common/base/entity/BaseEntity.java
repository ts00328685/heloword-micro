package com.heloword.common.base.entity;

import java.util.Date;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import com.heloword.common.listener.BaseEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EntityListeners(BaseEntityListener.class)
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_generator")
  private Long id;
  private Date createDate;
  private Date updateDate;
  private Long createBy;
  private Integer status;
  private Integer version;

}
