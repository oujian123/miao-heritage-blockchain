package com.miaoheritage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 * 用于处理AI鉴别等异步任务
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Value("${ai.async.core-pool-size}")
    private int corePoolSize;
    
    @Value("${ai.async.max-pool-size}")
    private int maxPoolSize;
    
    @Value("${ai.async.queue-capacity}")
    private int queueCapacity;
    
    @Value("${ai.async.thread-name-prefix}")
    private String threadNamePrefix;
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
} 