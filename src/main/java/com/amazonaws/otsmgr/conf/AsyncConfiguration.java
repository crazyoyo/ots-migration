package com.amazonaws.otsmgr.conf;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

    @Resource
    private MigrationConfig config;

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 核心线程数
        executor.setMaxPoolSize(5); // 最大线程数
        executor.setQueueCapacity(100); // 队列容量
        executor.setKeepAliveSeconds(60); // 非核心线程的存活时间
        executor.setAllowCoreThreadTimeOut(true); // 允许核心线程超时
        executor.setAwaitTerminationSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("otsmgr-async-");
        executor.initialize();
        return executor;
    }
}