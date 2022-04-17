package com.heloword.common.config.property;

import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import com.heloword.common.config.client.ClientConfig;

@Configuration
@PropertySources({
    // switch the active profile in this yml to change env
    // note that PropertySource is loaded after ApplicationContext is created, so some config might not work(ex. logging
    @PropertySource(name = "common-config-profile", value = "classpath:common-config-profile.yml", factory = PropertyConfig.YamlPropertySourceFactory.class),
    @PropertySource(name = "common-config", value = "classpath:common-config-${spring.profiles.active}.yml", factory = PropertyConfig.YamlPropertySourceFactory.class)
})
public class PropertyConfig {

  public static class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
      YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
      factory.setResources(encodedResource.getResource());
      Properties properties = factory.getObject();
      return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }
  }
}
