package com.mwmorin.wordlesolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class WordleSolverApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordleSolverApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/getnextwordjson").allowedOrigins("*");
				registry.addMapping("/getnextwordjson").allowedHeaders("*");
				registry.addMapping("/getnextwordjson").allowedMethods("*");
				registry.addMapping("/getnextwordjson").allowedOriginPatterns("*");
			}
		};
	}

}
