package com.miaoheritage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@EnableAsync
public class MiaoHeritageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiaoHeritageApplication.class, args);
    }
    
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
} 