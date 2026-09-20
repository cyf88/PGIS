package com.pgis.caseinfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "pgis.poi.enabled", havingValue = "true")
public class CaseLocationEnrichScheduler {

    private static final Logger log = LoggerFactory.getLogger(CaseLocationEnrichScheduler.class);

    private final CaseService caseService;
    private final PoiProperties properties;

    public CaseLocationEnrichScheduler(CaseService caseService, PoiProperties properties) {
        this.caseService = caseService;
        this.properties = properties;
    }

    @Scheduled(cron = "${pgis.poi.cron:0 0 * * * *}")
    public void enrichLocations() {
        log.info("定时任务触发: 开始补全案件经纬度，批量大小 {}", properties.getBatchSize());
        try {
            int updated = caseService.enrichLocations(properties.getBatchSize());
            log.info("定时任务结束: 本次更新案件坐标 {} 条", updated);
        } catch (Exception e) {
            log.error("定时补全案件经纬度失败", e);
        }
    }
}
