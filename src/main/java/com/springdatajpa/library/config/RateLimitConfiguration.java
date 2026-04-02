package com.springdatajpa.library.config;

import com.springdatajpa.library.support.MinuteBucketRateLimiter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfiguration {

    @Bean
    public MinuteBucketRateLimiter minuteBucketRateLimiter() {
        return new MinuteBucketRateLimiter();
    }
}
