package com.heloword.common.aspect;

import java.time.Instant;
import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Aspect
public class LogAspect {

  @Pointcut("execution(* com.heloword..*.rest..*(..))")
  public void pointcut() {
  }

  @Before("pointcut()")
  public void before(JoinPoint joinPoint) {
    log.info("=====calling rest method=====");
    log.info(getMethodName(joinPoint));
    log.info("args: " + Arrays.toString(joinPoint.getArgs()));
  }

  @Around("pointcut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    long startMillis = System.currentTimeMillis();
    try {
      return joinPoint.proceed();
    } finally {
      log.info("=====time elapsed: " + (System.currentTimeMillis() - startMillis) / 1000D + "s=====");
    }
  }


  @After("pointcut()")
  public void after(JoinPoint joinPoint) {
    log.info("=====calling rest method ends=====");
  }

  private static String getMethodName(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    return signature.getName();
  }
}
