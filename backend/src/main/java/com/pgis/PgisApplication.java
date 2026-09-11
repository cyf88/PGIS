package com.pgis;

import com.pgis.config.MapUiProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MapUiProperties.class)
@MapperScan({"com.pgis.deviceinfo", "com.pgis.devicelocation"})
public class PgisApplication {
    public static void main(String[] args) {
        SpringApplication.run(PgisApplication.class, args);
    }
}
