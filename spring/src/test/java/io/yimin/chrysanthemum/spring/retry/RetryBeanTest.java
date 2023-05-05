package io.yimin.chrysanthemum.spring.retry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RetryBean.class)
public class RetryBeanTest {

  @Autowired
  private RetryBean retryBean;

  @Test(expected = ArrayStoreException.class)
  public void httpInvocationWithoutParams() {
    retryBean.httpInvocationWithoutParams();
  }

  @Test
  public void httpInvocationWithParams() {
    retryBean.httpInvocationWithParams();
  }
}
