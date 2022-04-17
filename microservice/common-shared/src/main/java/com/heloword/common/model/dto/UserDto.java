package com.heloword.common.model.dto;

import java.util.Set;
import org.springframework.beans.BeanUtils;
import com.heloword.common.entity.user.MemberEntity;
import com.heloword.common.entity.user.RoleEntity;

import lombok.Data;

@Data
public class UserDto {

  private String username;
  private String fullname;
  private String nickname;
  private String picture;
  private String locale;
  private String email;
  private String googleToken;
  private String facebookToken;
  private Set<RoleEntity> roles;

  public static UserDto fromEntity(MemberEntity memberEntity){
    UserDto userDto = new UserDto();
    BeanUtils.copyProperties(memberEntity, userDto);
    return userDto;
  }

  @Data
  public static class Role {
    private String role;
    private String name;
  }
}
