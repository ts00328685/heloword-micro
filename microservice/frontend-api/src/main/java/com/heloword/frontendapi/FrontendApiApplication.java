package com.heloword.frontendapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.heloword.common.base.app.AbstractBaseApplication;

@SpringBootApplication()
@EnableScheduling
public class FrontendApiApplication extends AbstractBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontendApiApplication.class, args);
	}

}
