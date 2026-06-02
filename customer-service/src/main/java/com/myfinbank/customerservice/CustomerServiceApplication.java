package com.myfinbank.customerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

// Entry point for the Customer microservice. This service registers with Eureka and provides customer banking APIs.
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
