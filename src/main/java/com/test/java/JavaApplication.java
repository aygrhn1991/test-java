package com.test.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JavaApplication {

    public static void main(String[] args) {
        System.out.println("hello world!");
        SpringApplication.run(JavaApplication.class, args);
    }

}
