package com.heloword.common.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "MEMBER")
public class MemberEntity extends BaseEntity {

  private String username;
  private String password;
  private String email;

  private String googleToken;
  private String facebookToken;

}
