package com.heloword.record;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.heloword.common.base.app.AbstractBaseApplication;

@SpringBootApplication()
public class RecordApplication extends AbstractBaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordApplication.class, args);
	}

}
