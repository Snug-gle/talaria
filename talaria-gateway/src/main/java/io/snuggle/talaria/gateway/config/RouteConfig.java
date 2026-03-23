package io.snuggle.talaria.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("invest-route", r -> r
                        .path("/api/invest/**")
                        .uri("lb://talaria-invest"))
                .route("notify-route", r -> r
                        .path("/api/notify/**")
                        .uri("lb://talaria-notify"))
                .build();
    }
}
