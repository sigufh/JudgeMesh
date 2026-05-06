package com.judgemesh.submit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.judgemesh.api.client")
@ConfigurationPropertiesScan(basePackages = "com.judgemesh.submit")
public class SubmitApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubmitApplication.class, args);
    }
}
