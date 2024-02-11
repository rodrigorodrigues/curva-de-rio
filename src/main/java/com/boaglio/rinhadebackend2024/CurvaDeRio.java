package com.boaglio.rinhadebackend2024;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.boaglio.rinhadebackend2024")
public class CurvaDeRio {

	public static void main(String[] args) {
		SpringApplication.run(CurvaDeRio.class, args);
	}

}