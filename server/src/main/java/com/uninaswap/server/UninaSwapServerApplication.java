package com.uninaswap.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootApplication
public class UninaSwapServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UninaSwapServerApplication.class, args);
    }
    
    /**
     * Customizes the Jackson ObjectMapper to include JavaTimeModule for handling Java 8 date/time types.
     *
     * @return a Jackson2ObjectMapperBuilderCustomizer that registers the JavaTimeModule.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.modulesToInstall(new JavaTimeModule());
    }
}