package com.heloword.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.heloword.common.base.app.AbstractBaseApplication;

@SpringBootApplication()
public class AuthApplication extends AbstractBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
