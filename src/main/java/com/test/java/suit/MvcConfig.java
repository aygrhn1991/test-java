package com.test.java.suit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.savePath}")
    private String savePath;

    @Value("${file.saveContext}")
    private String saveContext;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(this.saveContext + "/**").addResourceLocations("file:/" + this.savePath + "/");
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
