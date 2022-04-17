package com.heloword.common.entity.user;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "role_seq")
@Table(name = "ROLE")
public class RoleEntity extends BaseEntity {

  private String role;
  private String name;

}
