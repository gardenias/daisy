package io.yimin.chrysanthemum.undertow;

import io.undertow.server.handlers.RequestLimitingHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.undertow.UndertowDeploymentInfoCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @Bean
  public UndertowDeploymentInfoCustomizer limit() {
    return deploymentInfo -> deploymentInfo.addInitialHandlerChainWrapper(handler -> {
      int availableProcessors = Runtime.getRuntime().availableProcessors();
      return new RequestLimitingHandler(availableProcessors * 20, availableProcessors * 10, handler);
    });
  }
}
