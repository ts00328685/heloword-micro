package com.heloword.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.heloword.common.base.app.AbstractBaseApplication;

@SpringBootApplication()
public class UserApplication extends AbstractBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}

}
