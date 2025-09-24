package com.conectaai.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.conectaai")
@EnableJpaRepositories(basePackages = "com.conectaai.repository")
@EntityScan(basePackages = "com.conectaai.domain")
public class ConectaAiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConectaAiGatewayApplication.class, args);
    }
}
