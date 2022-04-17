package com.heloword.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.heloword.common.config.ClientConfig;

@Configuration
public class PropertyConfiguration {

  @Autowired
  ClientConfig clientConfig;

  @Bean
  public ClientConfig clientConfig() {
    return clientConfig;
  }

}
