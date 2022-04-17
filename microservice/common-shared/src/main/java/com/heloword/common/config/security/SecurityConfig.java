package com.heloword.common.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.heloword.common.filter.ServiceAuthFilter;
import com.heloword.common.handler.CustomAccessDeniedHandler;
import com.heloword.common.type.MemberRole;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  ServiceAuthFilter jwtTokenFilter;

  @Autowired
  CustomAccessDeniedHandler customAccessDeniedHandler;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .cors().disable()
        .formLogin().disable()
        .httpBasic().disable()
        .anonymous().disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .exceptionHandling().accessDeniedHandler(customAccessDeniedHandler)
        .and()
        .authorizeRequests()
        .antMatchers("/auth/**").permitAll()
        .and()
        .authorizeRequests()
        .antMatchers("/**").hasAnyAuthority(MemberRole.ADMIN.getName(), MemberRole.FEIGN.getName())
        .and()
        .authorizeRequests()
        .anyRequest().authenticated();

    http.addFilterBefore(
        jwtTokenFilter,
        UsernamePasswordAuthenticationFilter.class
    );
  }

}
