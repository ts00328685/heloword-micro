package com.heloword.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.heloword.common.base.app.AbstractBaseApplication;

@SpringBootApplication
public class GatewayApplication extends AbstractBaseApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }

}
