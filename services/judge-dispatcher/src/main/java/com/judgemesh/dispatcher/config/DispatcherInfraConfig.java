package com.judgemesh.dispatcher.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class DispatcherInfraConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService dispatcherScheduler() {
        return Executors.newScheduledThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "dispatcher-scheduler");
            thread.setDaemon(true);
            return thread;
        });
    }
}
