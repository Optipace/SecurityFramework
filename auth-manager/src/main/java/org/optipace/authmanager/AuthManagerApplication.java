package org.optipace.authmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AuthManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthManagerApplication.class, args);
    }

}
