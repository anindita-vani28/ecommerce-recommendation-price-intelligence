package com.priceintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
public class PriceIntelApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceIntelApplication.class, args);
    }
}
