package com.chrizlove.vortexpay.api_gateway_service;

import com.chrizlove.vortexpay.api_gateway_service.security.SecurityRouteProperties;
import com.chrizlove.vortexpay.common_lib.idempotency.IdempotencyFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties(SecurityRouteProperties.class)
public class ApiGatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServiceApplication.class, args);
    }

}
