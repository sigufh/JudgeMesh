package com.judgemesh.dispatcher.config;

import io.etcd.jetcd.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class DispatcherEtcdConfig {

    private final DispatcherProperties properties;

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "judgemesh.dispatcher.mode", havingValue = "etcd")
    public Client dispatcherEtcdClient() {
        String[] endpoints = Arrays.stream(properties.getEtcd().getEndpoints().split("[,;\\s]+"))
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
        if (endpoints.length == 0) {
            endpoints = new String[]{"http://127.0.0.1:2379"};
        }
        return Client.builder().endpoints(endpoints).build();
    }
}
