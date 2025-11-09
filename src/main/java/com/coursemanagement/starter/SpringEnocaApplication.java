package com.coursemanagement.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.coursemanagement")
@EntityScan("com.coursemanagement.entity")
@EnableJpaRepositories("com.coursemanagement.repository")
public class SpringEnocaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringEnocaApplication.class, args);
    }
}