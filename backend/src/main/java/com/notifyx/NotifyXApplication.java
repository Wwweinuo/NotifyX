package com.notifyx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotifyXApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifyXApplication.class, args);
    }
}
