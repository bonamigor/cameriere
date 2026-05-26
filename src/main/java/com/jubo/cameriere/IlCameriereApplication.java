package com.jubo.cameriere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.jubo.cameriere.domain.repository")
@EnableTransactionManagement
public class IlCameriereApplication {

	public static void main(String[] args) {
		SpringApplication.run(IlCameriereApplication.class, args);
	}

}
