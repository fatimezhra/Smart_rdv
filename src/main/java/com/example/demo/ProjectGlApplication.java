package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.demo.config.AdminSeeder;

@SpringBootApplication(scanBasePackages = "com.example.demo")
public class ProjectGlApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectGlApplication.class, args);
	}

	@Bean
	CommandLineRunner seedAdmin(AdminSeeder adminSeeder) {
		return args -> adminSeeder.seed();
	}
}
