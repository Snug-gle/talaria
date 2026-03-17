package io.github.snuggle.talaria.notify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TalariaNotifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalariaNotifyApplication.class, args);
    }
}
