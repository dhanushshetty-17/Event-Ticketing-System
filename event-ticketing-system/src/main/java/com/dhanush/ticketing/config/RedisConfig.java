
package com.dhanush.ticketing.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(
            @Value("${redis.address:redis://localhost:6379}")
            String redisAddress) {

        Config config = new Config();

        config.useSingleServer()
                .setAddress(redisAddress)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(24);

        return Redisson.create(config);
    }
}
