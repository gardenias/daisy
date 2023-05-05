package io.yimin.chrysanthemum.spring.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = "io.yimin.chrysanthemum.spring.start")
public class Main {
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @Component
  class Brain {

    @PostConstruct
    public void init() {
      throw new RuntimeException("Post Error");
    }
  }

  @Component
  class Person {

    //    @Autowired
    private Brain brain;
  }

}
