package com.heloword.common.base.app;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// add this component scan so that it will scan the shared common packages
@ComponentScan({"com.heloword"})
@EnableJpaRepositories("com.heloword.common.repo")
@EnableFeignClients("com.heloword.common.feignclient")
@EntityScan("com.heloword.common.entity")
public abstract class AbstractBaseApplication {
}
