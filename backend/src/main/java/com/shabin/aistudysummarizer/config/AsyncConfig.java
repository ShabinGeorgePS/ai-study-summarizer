package com.shabin.aistudysummarizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous processing and caching.
 * Enables @Async and @Cacheable annotations throughout the application.
 */
@Configuration
@EnableAsync
@EnableCaching
public class AsyncConfig {

    /**
     * Configure thread pool for async tasks
     * Used for background processing like summary generation
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);           // Minimum threads
        executor.setMaxPoolSize(10);           // Maximum threads
        executor.setQueueCapacity(100);        // Task queue size
        executor.setThreadNamePrefix("summary-async-");
        executor.setAwaitTerminationSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
