package com.example.niharika.spring.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.niharika.spring.ai.chat", "com.example.niharika.spring.ai.rag"})
public class SpringAIApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAIApplication.class, args);
	}

}
