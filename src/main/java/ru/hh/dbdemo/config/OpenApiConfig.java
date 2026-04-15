package ru.hh.dbdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI lectureOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("JDBC to Hibernate Demo")
            .description("Commit 1: vanilla JDBC baseline")
            .version("v1"));
  }
}
