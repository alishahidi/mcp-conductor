package net.alishahidi.mcpconductor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableRetry
public class McpConductorApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpConductorApplication.class, args); // Fixed class reference
    }
}