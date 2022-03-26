package com.heloword.word;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.heloword.common.base.app.AbstractBaseApplication;

@SpringBootApplication()
public class WordApplication extends AbstractBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordApplication.class, args);
	}

}
