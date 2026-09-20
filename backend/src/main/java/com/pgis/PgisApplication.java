package com.pgis;

import com.pgis.caseinfo.PoiProperties;
import com.pgis.config.MapUiProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({MapUiProperties.class, PoiProperties.class})
@EnableScheduling
@MapperScan({"com.pgis.deviceinfo", "com.pgis.devicelocation", "com.pgis.alarm", "com.pgis.caseinfo"})
public class PgisApplication {
    public static void main(String[] args) {
        SpringApplication.run(PgisApplication.class, args);
    }
}
