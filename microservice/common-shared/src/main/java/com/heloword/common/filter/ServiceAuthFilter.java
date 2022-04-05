package com.heloword.common.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.heloword.common.entity.MemberEntity;
import com.heloword.common.entity.RoleEntity;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.AuthType;
import com.heloword.common.type.MemberRole;
import com.heloword.common.type.ResponseCode;
import com.heloword.common.util.UserSessionUtil;
import com.heloword.common.util.Util;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceAuthFilter extends OncePerRequestFilter {

  @Autowired
  private UserSessionUtil userSessionUtil;

  @Value("${feign.api-key}")
  private String feignApiKey;

  @Autowired
  Environment environment;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain
  ) throws ServletException, IOException {

    try {
      handleServiceAuthentication(request, response, chain);
    } catch (Exception e) {
      log.error("unauthorized request caught", e);
      throw HeloServiceException.of(ResponseCode.INSUFFICIENT_AUTHORITY);
    }
  }

  private void handleServiceAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

    String idToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[]{}))
        .filter(c -> StringUtils.equals(AuthType.GOOGLE_ID_TOKEN.getKey(), c.getName()))
        .findAny()
        .map(Cookie::getValue)
        .orElse(StringUtils.EMPTY);

    if (Util.isLocalEnv(environment)) {
      idToken = StringUtils.split(request.getHeader(HttpHeaders.AUTHORIZATION), StringUtils.SPACE)[1];
    }

    final String headerFeignApiKey = request.getHeader(AuthType.FEIGN_API_KEY.getKey());

    Set<GrantedAuthority> additionalAuths = new HashSet<>();

    if (StringUtils.equals(headerFeignApiKey, this.feignApiKey)) {
      log.debug("called by feign client");
      additionalAuths.add(new SimpleGrantedAuthority(MemberRole.FEIGN.getName()));
    }

    UserDetails userDetails = CustomUserDetails.of(userSessionUtil.getUserFromSession(idToken), additionalAuths);

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails,
        userDetails.getPassword(),
        userDetails.getAuthorities()
    );

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(request, response);
  }

  @Data
  @SuperBuilder
  public static class CustomUserDetails extends MemberEntity implements UserDetails {

    private Collection<? extends GrantedAuthority> grantedAuthority;

    public static CustomUserDetails of(Optional<MemberEntity> memberEntity, Set<GrantedAuthority> additionalAuth) {
      CustomUserDetails userDetails = CustomUserDetails.of(memberEntity);
      additionalAuth.addAll(userDetails.getAuthorities());
      userDetails.grantedAuthority = additionalAuth;
      userDetails.setGrantedAuthority(additionalAuth);
      return userDetails;
    }

    public static CustomUserDetails of(Optional<MemberEntity> memberEntity) {
      if (memberEntity.isPresent()) {
        MemberEntity existingMember = memberEntity.get();
        Set<GrantedAuthority> authorities = existingMember.getRoles().stream()
            .map(RoleEntity::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

        return CustomUserDetails.builder()
            .grantedAuthority(authorities)
            .username(existingMember.getUsername())
            .status(existingMember.getStatus())
            .build();

      } else {

        return CustomUserDetails.builder()
            .grantedAuthority(Set.of(new SimpleGrantedAuthority(MemberRole.UNREGISTERED_MEMBER.getName())))
            .status(1)
            .build();
      }

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
      return grantedAuthority;
    }

    @Override
    public boolean isAccountNonExpired() {
      return getStatus() == 1;
    }

    @Override
    public boolean isAccountNonLocked() {
      return getStatus() == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
      return true;
    }

    @Override
    public boolean isEnabled() {
      return getStatus() == 1;
    }
  }

}
