package com.jubo.cameriere.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.jubo.cameriere.domain.repository")
@EntityScan(basePackages = "com.jubo.cameriere.domain.model")
public class JpaConfig {
}
