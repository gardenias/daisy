package io.yimin.chrysanthemum.spring.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@EnableRetry
@Retryable
public class RetryBean {
  private final AtomicInteger counter = new AtomicInteger();
  private final Random random = new Random(System.currentTimeMillis());

  public void httpInvocationWithoutParams() {
    randomThrowTimeOutException();
  }

  public void httpInvocationWithParams() {
//    log.info("times {}", counter.getAndIncrement());
  }

  private void randomThrowTimeOutException() {
    int anInt = random.nextInt();
//    log.info("times {}", counter.getAndIncrement());
    throw new ArrayStoreException(anInt + "");
  }
}
