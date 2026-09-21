package com.sl.express.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@org.springframework.context.annotation.Configuration
public class RedisScriptConfig {

    @Bean
    public RedisScript<Long> orderCreateIdempotentScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/order-create-idempotent.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
