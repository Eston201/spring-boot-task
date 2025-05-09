package com.estonnaicker.tasks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.any())              
          .paths(PathSelectors.any())
          .paths(PathSelectors.regex("/error.*").negate())                        
          .build()
          .apiInfo(apiInfo());                                           
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Simple Task API")
                .description("CRUD API documentation for tasks")
                .version("1.0.0")
                .build();
    }
}