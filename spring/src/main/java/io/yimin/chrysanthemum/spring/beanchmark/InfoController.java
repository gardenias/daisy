package io.yimin.chrysanthemum.spring.beanchmark;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

  @GetMapping("/hello")
  public String hello() {
    return "Hello, gus";
  }
}
