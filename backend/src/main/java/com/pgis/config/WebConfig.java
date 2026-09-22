package com.pgis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/device-info/**").setViewName("forward:/index.html");
        registry.addViewController("/device-location/**").setViewName("forward:/index.html");
        registry.addViewController("/alarm/**").setViewName("forward:/index.html");
        registry.addViewController("/case/**").setViewName("forward:/index.html");
        registry.addViewController("/map/**").setViewName("forward:/index.html");
    }
}
