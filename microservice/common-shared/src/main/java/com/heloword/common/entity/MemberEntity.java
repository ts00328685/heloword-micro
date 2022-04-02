package com.heloword.common.entity;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.springframework.data.redis.core.RedisHash;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heloword.common.base.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@RedisHash("MEMBER")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "member_seq")
@Table(name = "MEMBER")
public class MemberEntity extends BaseEntity {

  @Column(unique = true)
  private String username;
  private String fullname;
  private String nickname;
  @Column(length = 2048)
  private String picture;
  private String locale;
  private String password;
  @Column(unique = true)
  private String email;
  @JsonIgnore
  @Column(length = 4096)
  private String googleToken;
  @JsonIgnore
  @Column(length = 4096)
  private String facebookToken;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "MEMBER_ROLE",
      joinColumns = {@JoinColumn(name = "MEMBER_ID")},
      inverseJoinColumns = {@JoinColumn(name = "ROLE_ID")})
  private Set<RoleEntity> roles;

}
