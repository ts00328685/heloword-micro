package com.heloword.common.config;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.google.gson.Gson;

@Configuration
@PropertySources({
    // switch the active profile in this yml to change env
    // note that PropertySource is loaded after ApplicationContext is created, so some config might not work(ex. logging
    @PropertySource(name = "common-config-profile", value = "classpath:common-config-profile.yml", factory = YamlPropertySourceFactory.class),
    @PropertySource(name = "common-config", value = "classpath:common-config-${spring.profiles.active}.yml", factory = YamlPropertySourceFactory.class)
})
public class ClientConfig {

  @Bean
  public HttpMessageConverters messageConverters() {
    return new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

  @Bean
  public Gson gson() {
    return new Gson();
  }

}
